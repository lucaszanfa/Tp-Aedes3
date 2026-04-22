# Documentacao do Projeto - Versao para Word

## 1. Identificacao do projeto

**Nome do projeto:** Loja Online  
**Disciplina:** Trabalho Pratico  
**Tema:** Sistema de gerenciamento de clientes, produtos, cupons e pedidos com persistencia em arquivos binarios

## 2. Descricao do problema

O projeto representa uma loja online capaz de cadastrar e gerenciar clientes, produtos, cupons promocionais e pedidos. O objetivo principal foi desenvolver uma aplicacao sem uso de banco de dados relacional, utilizando arquivos binarios para armazenar os registros de forma persistente entre diferentes execucoes do sistema.

O sistema foi desenvolvido em Java e segue a arquitetura MVC + DAO. Alem da persistencia local em disco, a aplicacao implementa indices primarios para todas as entidades e um relacionamento 1:N entre cliente e pedido utilizando hash extensivel, conforme exigido pelo trabalho.

A interface principal de uso foi implementada em HTML e CSS, servida localmente por um servidor HTTP embutido. Dessa forma, o sistema nao depende de console como interface final e permite demonstrar as operacoes de forma visual.

## 3. Objetivo do trabalho

Os principais objetivos do projeto foram:

- implementar o CRUD completo das entidades modeladas;
- armazenar os dados em arquivos binarios com cabecalho;
- manter os dados persistidos entre execucoes;
- implementar indices primarios em disco;
- implementar o relacionamento 1:N com hash extensivel;
- manter a arquitetura definida na fase anterior;
- documentar as decisoes de projeto e os diagramas refinados.

## 4. Entidades do sistema

O sistema foi estruturado em torno de quatro entidades principais:

**Cliente**
- identificador unico;
- nome;
- email;
- conjunto de telefones.

**Produto**
- identificador unico;
- nome;
- preco;
- quantidade em estoque.

**Cupom**
- identificador unico;
- codigo do cupom;
- percentual de desconto;
- indicador de ativo ou inativo.

**Pedido**
- identificador unico;
- identificador do cliente;
- identificador do cupom;
- data do pedido;
- valor total;
- lista de produtos e quantidades.

Na modelagem conceitual tambem aparece a entidade ItemPedido, representando a relacao entre pedido e produto. Porem, na implementacao final essa estrutura foi incorporada ao proprio registro de pedido por meio de vetores de IDs de produtos e respectivas quantidades.

## 5. Requisitos funcionais atendidos

O sistema atende aos seguintes requisitos funcionais:

- cadastrar clientes;
- consultar clientes por ID;
- atualizar clientes;
- excluir clientes logicamente;
- listar clientes ativos;
- cadastrar produtos;
- consultar produtos por ID;
- atualizar produtos;
- excluir produtos logicamente;
- listar produtos ativos;
- cadastrar cupons;
- consultar cupons por ID;
- atualizar cupons;
- excluir cupons logicamente;
- listar cupons ativos;
- criar pedidos;
- consultar pedidos por ID;
- atualizar pedidos;
- excluir pedidos logicamente;
- listar pedidos ativos;
- associar cupons ativos a pedidos;
- listar pedidos de um cliente por meio do relacionamento 1:N.

## 6. Requisitos nao funcionais atendidos

O projeto tambem atende aos seguintes requisitos nao funcionais:

- interface implementada em HTML e CSS;
- persistencia feita em arquivos binarios;
- indices mantidos em disco;
- arquitetura organizada em camadas;
- validacao de entradas e tratamento de erros;
- execucao local via servidor HTTP.

## 7. Arquitetura adotada

O projeto foi estruturado com base no padrao MVC + DAO.

**Model**  
Camada responsavel pelas classes de dominio, que representam as entidades persistidas pelo sistema.

**DAO**  
Camada responsavel pela gravacao, leitura, atualizacao e exclusao logica dos registros em disco, alem da manutencao dos indices.

**Controller**  
Camada responsavel pelas regras de negocio, validacoes e verificacao de integridade referencial.

**View**  
Camada responsavel pela interface HTML/CSS exibida no navegador.

**Main**  
Camada responsavel pela inicializacao do servidor HTTP e pelo roteamento das requisicoes.

Essa organizacao foi mantida porque facilita manutencao, separa responsabilidades e deixa mais claro onde cada requisito foi implementado.

## 8. Decisoes de projeto

### 8.1 Persistencia por entidade

Foi adotado um arquivo binario separado para cada entidade principal do sistema. Essa decisao permite organizar melhor os dados, manter a independência entre os modulos e simplificar a reconstrucao dos indices.

Os arquivos principais de dados sao:

- `clientes.db`
- `produtos.db`
- `cupons.db`
- `pedidos.db`

### 8.2 Estrutura fisica dos registros

Cada arquivo binario possui um cabecalho inicial com o ultimo ID gerado. Depois do cabecalho, cada registro e armazenado contendo:

- uma lapide booleana;
- o tamanho do payload;
- o payload serializado da entidade.

A lapide indica se o registro esta ativo ou se foi excluido logicamente.

### 8.3 Exclusao logica

A exclusao logica foi escolhida para atender ao enunciado e para evitar a remocao fisica imediata dos registros. Quando um registro e removido, ele continua ocupando espaco no arquivo, mas deixa de ser retornado nas consultas e listagens, alem de ser removido dos indices.

### 8.4 Serializacao dos registros

Cada classe de dominio implementa metodos proprios para conversao entre objeto e vetor de bytes. Essa escolha foi feita para manter a serializacao explicita e controlada, em vez de depender de uma solucao generica menos transparente.

Para atributos textuais e multivalorados, foi utilizada uma classe auxiliar dedicada ao tratamento binario de strings.

### 8.5 Indices primarios

Todas as tabelas do sistema possuem indice primario baseado na chave primaria. O indice foi implementado com hash extensivel persistido em disco.

Nesse indice:

- a chave e a PK inteira da entidade;
- o valor armazenado e a posicao do registro no arquivo de dados.

Essa escolha melhora a eficiencia da busca por ID e evita varredura sequencial do arquivo principal sempre que um registro precisa ser consultado.

### 8.6 Relacionamento 1:N com hash extensivel

O relacionamento entre cliente e pedido foi implementado por meio de hash extensivel combinado com lista invertida.

Nesse modelo:

- a chave de busca e o ID do cliente;
- o valor do hash aponta para o inicio de uma lista de pedidos;
- a lista armazena os IDs dos pedidos associados ao cliente.

Essa estrutura foi escolhida para permitir acesso eficiente aos pedidos de um cliente sem percorrer todos os registros de pedido.

### 8.7 Itens do pedido

Na modelagem conceitual, os itens do pedido aparecem como entidade propria. Na implementacao, optou-se por manter os itens dentro do registro de pedido, com dois vetores paralelos:

- um vetor de IDs de produtos;
- um vetor de quantidades.

Essa decisao reduziu a complexidade da persistencia e simplificou a manipulacao dos pedidos dentro do escopo do trabalho.

### 8.8 Interface web local

Foi escolhida uma interface web local servida por `HttpServer`. Essa decisao atendeu ao requisito de front-end obrigatorio e permitiu uma apresentacao mais amigavel do sistema, sem dependencia de frameworks externos ou banco de dados.

## 9. Regras de negocio implementadas

As principais regras de negocio do sistema sao:

- um pedido so pode ser criado para um cliente existente;
- um pedido so pode conter produtos existentes;
- a quantidade de cada item deve ser positiva;
- o estoque precisa ser suficiente para a criacao ou atualizacao de pedido;
- a criacao de pedido reduz o estoque dos produtos envolvidos;
- a atualizacao de pedido recalcula o estoque considerando os itens antigos e os novos;
- um cupom so pode ser associado se existir e estiver ativo;
- um pedido nao pode receber mais de um cupom;
- registros excluidos logicamente nao aparecem nas listagens de ativos;
- a navegacao do relacionamento cliente para pedidos considera apenas pedidos ativos.

## 10. Diagramas do projeto

Os diagramas definidos na fase anterior foram mantidos e refinados para refletir a implementacao final.

### 10.1 Diagrama de Caso de Uso

O diagrama de caso de uso representa os papeis de negocio e as funcionalidades principais do sistema. Os atores considerados foram:

- Administrador;
- Cliente.

Os principais casos de uso documentados sao:

- cadastrar cliente;
- gerenciar produto;
- gerenciar cupom;
- criar pedido;
- associar cupom a pedido;
- listar registros ativos;
- atualizar registro;
- consultar por ID;
- excluir logicamente.

### 10.2 Diagrama Entidade-Relacionamento

O DER representa as entidades Cliente, Produto, Cupom e Pedido, alem da entidade conceitual ItemPedido. O relacionamento mais importante para a etapa atual e o relacionamento 1:N entre Cliente e Pedido.

O diagrama mostra:

- um cliente pode realizar varios pedidos;
- um pedido pode conter varios itens;
- um produto pode aparecer em varios itens de pedido;
- um pedido pode ter zero ou um cupom associado.

### 10.3 Diagrama de Arquitetura em Camadas

O diagrama arquitetural evidencia a separacao entre interface, controle, persistencia e dados, mostrando o fluxo entre View, Main, Controllers, DAOs, arquivos de dados e arquivos de indice.

## 11. Armazenamento dos indices em disco

Cada tabela possui arquivos especificos para o indice primario. O armazenamento do hash extensivel foi dividido em arquivos de diretorio e arquivos de buckets.

No diretorio ficam:

- profundidade global;
- capacidade dos buckets;
- ponteiros para os buckets.

Nos buckets ficam:

- profundidade local;
- quantidade de entradas ativas;
- pares de chave e endereco.

No caso do relacionamento 1:N, alem dos arquivos de diretorio e buckets, existe um arquivo adicional que armazena a lista invertida dos pedidos de cada cliente.

## 12. Acesso ao relacionamento 1:N

O acesso ao relacionamento entre cliente e pedidos ocorre da seguinte forma:

1. o sistema recebe o ID do cliente;
2. o hash extensivel localiza o bucket correspondente;
3. o bucket retorna o ponteiro para a lista invertida;
4. a lista invertida fornece os IDs dos pedidos vinculados;
5. cada pedido e recuperado pelo indice primario do arquivo de pedidos.

Essa estrategia evita varredura sequencial em todos os pedidos e atende ao requisito especifico do trabalho.

## 13. Validacao de entradas e tratamento de erros

O sistema realiza validacao de entradas para evitar inconsistencias comuns. Entre os casos tratados estao:

- busca por chave inexistente;
- exclusao de registro nao encontrado;
- campos obrigatorios vazios;
- valores numericos invalidos;
- quantidades negativas;
- preco negativo;
- estoque negativo;
- tentativa de usar cupom inexistente ou inativo;
- tentativa de criar pedido para cliente inexistente;
- tentativa de criar pedido com estoque insuficiente.

As mensagens de erro sao retornadas pela interface de forma compreensivel para o usuario.

## 14. Interface do sistema

O sistema possui paginas para:

- clientes;
- produtos;
- cupons;
- pedidos.

Em cada modulo, a interface permite operacoes de cadastro, consulta, atualizacao, exclusao logica e listagem. No modulo de pedidos tambem existe a funcionalidade de associar cupom e consultar pedidos por cliente.

## 15. Estrutura do repositorio

O repositorio foi organizado nas seguintes pastas:

- `Model`
- `DAO`
- `Controller`
- `View`
- `Main`
- `Util`
- `docs`
- `data`

Essa organizacao acompanha a arquitetura do projeto e facilita a localizacao dos componentes.

## 16. Compilacao e execucao

Para compilar o projeto:

`javac --release 8 Main\App.java`

Para executar:

`java Main.App`

Depois disso, a aplicacao pode ser acessada no navegador pelo endereco:

`http://localhost:18080`

## 17. Conclusao

O projeto Loja Online atende ao escopo proposto para a etapa atual ao combinar arquitetura em camadas, persistencia binaria, indices primarios em disco, exclusao logica, interface web local e relacionamento 1:N com hash extensivel.

As decisoes de projeto priorizaram clareza, organizacao e aderencia aos requisitos da disciplina. Os diagramas da fase anterior foram preservados e refinados para refletir a implementacao final, e a documentacao foi organizada de modo a facilitar tanto a avaliacao tecnica quanto a apresentacao do trabalho.
