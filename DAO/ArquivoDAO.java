package DAO;

import Model.Registro;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;

public class ArquivoDAO<T extends Registro> {

    private static final int HEADER_SIZE = 4;
    private static final int PRIMARY_BUCKET_CAPACITY = 4;
    private final String path;
    private final RegistroFactory<T> factory;
    private final ExtensibleHashIndex primaryIndex;

    public ArquivoDAO(String path, RegistroFactory<T> factory) throws IOException {
        this.path = path;
        this.factory = factory;
        initFile();
        this.primaryIndex = new ExtensibleHashIndex(path + ".pk.dir.db", path + ".pk.buckets.db", PRIMARY_BUCKET_CAPACITY);
        rebuildPrimaryIndex();
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
            long position = raf.length();
            raf.seek(position);
            raf.writeBoolean(false);
            raf.writeInt(data.length);
            raf.write(data);
            primaryIndex.put(novoId, position);
            return registro;
        }
    }

    public synchronized T read(int id) throws IOException {
        Long position = primaryIndex.get(id);
        if (position == null) {
            return null;
        }
        try (RandomAccessFile raf = new RandomAccessFile(path, "r")) {
            return readAtPosition(raf, position);
        }
    }

    public synchronized boolean update(T registroAtualizado) throws IOException {
        Long position = primaryIndex.get(registroAtualizado.getId());
        if (position == null) {
            return false;
        }
        try (RandomAccessFile raf = new RandomAccessFile(path, "rw")) {
            raf.seek(position);
            boolean lapide = raf.readBoolean();
            int size = raf.readInt();
            long payloadPosition = raf.getFilePointer();

            if (lapide || size < 0) {
                primaryIndex.remove(registroAtualizado.getId());
                return false;
            }

            byte[] novo = registroAtualizado.toByteArray();
            if (novo.length <= size) {
                raf.seek(payloadPosition);
                raf.write(novo);
                if (novo.length < size) {
                    raf.write(new byte[size - novo.length]);
                }
            } else {
                raf.seek(position);
                raf.writeBoolean(true);
                long newPosition = raf.length();
                raf.seek(newPosition);
                raf.writeBoolean(false);
                raf.writeInt(novo.length);
                raf.write(novo);
                primaryIndex.put(registroAtualizado.getId(), newPosition);
            }
            return true;
        }
    }

    public synchronized boolean delete(int id) throws IOException {
        Long position = primaryIndex.get(id);
        if (position == null) {
            return false;
        }
        try (RandomAccessFile raf = new RandomAccessFile(path, "rw")) {
            raf.seek(position);
            boolean lapide = raf.readBoolean();
            if (lapide) {
                primaryIndex.remove(id);
                return false;
            }
            raf.seek(position);
            raf.writeBoolean(true);
            primaryIndex.remove(id);
            return true;
        }
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

    protected synchronized void rebuildPrimaryIndex() throws IOException {
        primaryIndex.clear();
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
                    primaryIndex.put(registro.getId(), position);
                }
            }
        }
    }

    protected String getPath() {
        return path;
    }

    private T readAtPosition(RandomAccessFile raf, long position) throws IOException {
        if (position < HEADER_SIZE || position >= raf.length()) {
            return null;
        }
        raf.seek(position);
        boolean lapide = raf.readBoolean();
        int size = raf.readInt();
        if (lapide || size < 0) {
            return null;
        }
        byte[] data = new byte[size];
        raf.readFully(data);
        T registro = factory.create();
        registro.fromByteArray(data);
        return registro;
    }
}
