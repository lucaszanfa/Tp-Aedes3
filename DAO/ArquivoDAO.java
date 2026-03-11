package DAO;

import Model.Registro;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;

public class ArquivoDAO<T extends Registro> {

    private static final int HEADER_SIZE = 4;
    private final String path;
    private final RegistroFactory<T> factory;

    public ArquivoDAO(String path, RegistroFactory<T> factory) throws IOException {
        this.path = path;
        this.factory = factory;
        initFile();
    }

    private void initFile() throws IOException {
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        if (!file.exists()) {
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                raf.writeInt(0);
            }
        } else if (file.length() < HEADER_SIZE) {
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                raf.setLength(0);
                raf.writeInt(0);
            }
        }
    }

    public synchronized T create(T registro) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(path, "rw")) {
            raf.seek(0);
            int ultimoId = raf.readInt();
            int novoId = ultimoId + 1;
            registro.setId(novoId);

            byte[] data = registro.toByteArray();
            raf.seek(0);
            raf.writeInt(novoId);
            raf.seek(raf.length());
            raf.writeBoolean(false);
            raf.writeInt(data.length);
            raf.write(data);
            return registro;
        }
    }

    public synchronized T read(int id) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
            raf.seek(HEADER_SIZE);
            while (raf.getFilePointer() < raf.length()) {
                long position = raf.getFilePointer();
                boolean lapide = raf.readBoolean();
                int size = raf.readInt();

                if (size < 0) {
                    break;
                }

                byte[] data = new byte[size];
                raf.readFully(data);

                if (!lapide) {
                    T registro = factory.create();
                    registro.fromByteArray(data);
                    if (registro.getId() == id) {
                        return registro;
                    }
                }

                if (raf.getFilePointer() <= position) {
                    break;
                }
            }
        }
        return null;
    }

    public synchronized boolean update(T registroAtualizado) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(path, "rw")) {
            raf.seek(HEADER_SIZE);
            while (raf.getFilePointer() < raf.length()) {
                long posRegistro = raf.getFilePointer();
                boolean lapide = raf.readBoolean();
                int size = raf.readInt();
                long posPayload = raf.getFilePointer();

                if (size < 0) {
                    return false;
                }

                byte[] data = new byte[size];
                raf.readFully(data);

                if (!lapide) {
                    T atual = factory.create();
                    atual.fromByteArray(data);
                    if (atual.getId() == registroAtualizado.getId()) {
                        byte[] novo = registroAtualizado.toByteArray();
                        if (novo.length <= size) {
                            raf.seek(posPayload);
                            raf.write(novo);
                            if (novo.length < size) {
                                raf.write(new byte[size - novo.length]);
                            }
                        } else {
                            raf.seek(posRegistro);
                            raf.writeBoolean(true);
                            raf.seek(raf.length());
                            raf.writeBoolean(false);
                            raf.writeInt(novo.length);
                            raf.write(novo);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public synchronized boolean delete(int id) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(path, "rw")) {
            raf.seek(HEADER_SIZE);
            while (raf.getFilePointer() < raf.length()) {
                long posRegistro = raf.getFilePointer();
                boolean lapide = raf.readBoolean();
                int size = raf.readInt();

                if (size < 0) {
                    return false;
                }

                byte[] data = new byte[size];
                raf.readFully(data);

                if (!lapide) {
                    T registro = factory.create();
                    registro.fromByteArray(data);
                    if (registro.getId() == id) {
                        raf.seek(posRegistro);
                        raf.writeBoolean(true);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public synchronized List<T> listActive() throws IOException {
        List<T> list = new LinkedList<>();
        try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
            raf.seek(HEADER_SIZE);
            while (raf.getFilePointer() < raf.length()) {
                boolean lapide = raf.readBoolean();
                int size = raf.readInt();

                if (size < 0) {
                    break;
                }

                byte[] data = new byte[size];
                raf.readFully(data);

                if (!lapide) {
                    T registro = factory.create();
                    registro.fromByteArray(data);
                    list.add(registro);
                }
            }
        }
        return list;
    }
}
