package Model; // Define o pacote onde a classe está organizada

import java.io.*; // Importa classes para manipulação de entrada e saída de dados (streams e bytes)

public class Produto { // Declara a classe Produto
    
    private int id; // Atributo que armazena o identificador do produto
    private String nome; // Atributo que armazena o nome do produto
    private double preco; // Atributo que armazena o preço do produto
    private int estoque; // Atributo que armazena a quantidade em estoque

    public Produto() { // Construtor vazio da classe
    } // Fim do construtor vazio

    public Produto(int id, String nome, double preco, int estoque) { // Construtor com parâmetros
        this.id = id; // Atribui o valor recebido ao atributo id
        this.nome = nome; // Atribui o valor recebido ao atributo nome
        this.preco = preco; // Atribui o valor recebido ao atributo preco
        this.estoque = estoque; // Atribui o valor recebido ao atributo estoque
    } // Fim do construtor com parâmetros

    public void setId(int id) { // Método setter para alterar o id
        this.id = id; // Define o novo valor do id
    } // Fim do setter

    public int getId() { // Método getter para retornar o id
        return id; // Retorna o valor do id
    } // Fim do getter

    public void setNome(String nome) { // Setter para alterar o nome
        this.nome = nome; // Define o novo nome
    } // Fim do setter

    public String getNome() { // Getter para retornar o nome
        return nome; // Retorna o nome
    } // Fim do getter

    public void setPreco(double preco) { // Setter para alterar o preço
        this.preco = preco; // Define o novo preço
    } // Fim do setter

    public double getPreco() { // Getter para retornar o preço
        return preco; // Retorna o preço
    } // Fim do getter

    public void setEstoque(int estoque) { // Setter para alterar o estoque
        this.estoque = estoque; // Define o novo estoque
    } // Fim do setter

    public int getEstoque() { // Getter para retornar o estoque
        return estoque; // Retorna o estoque
    } // Fim do getter


    public byte[] toByteArray() throws IOException { // Método que converte o objeto em array de bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); // Cria um fluxo de saída de bytes na memória
        DataOutputStream dos = new DataOutputStream(baos); // Permite escrever tipos primitivos no fluxo de bytes

        dos.writeInt(id); // Escreve o id como 4 bytes
        dos.writeUTF(nome); // Escreve a String nome em formato UTF (inclui tamanho + bytes)
        dos.writeDouble(preco); // Escreve o preço como 8 bytes (double)
        dos.writeInt(estoque); // Escreve o estoque como 4 bytes

        return baos.toByteArray(); // Retorna todos os dados convertidos em array de bytes
    } // Fim do método toByteArray

    public void fromByteArray(byte[] ba) throws IOException { // Método que reconstrói o objeto a partir de um array de bytes
        ByteArrayInputStream bais = new ByteArrayInputStream(ba); // Cria um fluxo de entrada usando o array de bytes
        DataInputStream dis = new DataInputStream(bais); // Permite ler tipos primitivos do fluxo de bytes

        id = dis.readInt(); // Lê 4 bytes e converte para int (id)
        nome = dis.readUTF(); // Lê os bytes da String e reconstrói o nome
        preco = dis.readDouble(); // Lê 8 bytes e converte para double (preço)
        estoque = dis.readInt(); // Lê 4 bytes e converte para int (estoque)
    } // Fim do método fromByteArray

    @Override // Indica que este método sobrescreve um método da classe Object
    public String toString() { // Método que define como o objeto será exibido em formato texto
        return "Produto [id=" + id + // Concatena o id
        ", nome=" + nome + // Concatena o nome
        ", preco=" + preco + // Concatena o preço
        ", estoque=" + estoque +"]"; // Concatena o estoque e fecha a String
    } // Fim do método toString
} // Fim da classe Produto