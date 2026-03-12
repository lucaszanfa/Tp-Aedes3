# Loja Online - TP

Implementacao em Java seguindo MVC + DAO com persistencia em arquivos binarios, cabecalho de ultimo ID e exclusao logica por lapide. O sistema foi tematizado como uma **Loja Online** com interface web local servida por `HttpServer`.

## Estrutura
- `Model/`: entidades de dominio e interface `Registro`.
- `DAO/`: persistencia em arquivo binario e fabrica de registros.
- `Controller/`: validacoes e regras de negocio.
- `View/`: geracao de HTML/CSS.
- `Main/`: inicializacao do servidor HTTP e roteamento.
- `Util/`: utilitarios de serializacao, incluindo strings multivaloradas.
- `docs/`: documentacao do trabalho.
- `data/`: arquivos binarios gerados em runtime.

## Funcionalidades implementadas
- Gestao de clientes: cadastrar, listar, consultar por ID, atualizar e excluir logicamente.
- Gestao de produtos: cadastrar, listar, consultar por ID, atualizar e excluir logicamente.
- Gestao de cupons: cadastrar, listar, consultar por ID, atualizar e excluir logicamente.
- Gestao de pedidos: criar pedido com multiplos produtos, listar, consultar por ID, excluir logicamente e associar cupom ativo.
- Atualizacao automatica de estoque ao criar pedidos.
- Interface HTML/CSS unica, acessada pelo navegador.

## Persistencia
Cada arquivo `*.db` possui:
- cabecalho `int` com o ultimo ID gerado;
- lapide `boolean` por registro;
- tamanho do payload `int`;
- payload serializado da entidade.

O bloco de strings foi implementado em `Util/BinaryStringIO.java` seguindo a regra:
- `2 bytes`: quantidade de strings do bloco.
- para cada string:
- `4 bytes`: tamanho em bytes UTF-8.
- `N bytes`: conteudo da string.

Arquivos gerados:
- `data/clientes.db`
- `data/produtos.db`
- `data/cupons.db`
- `data/pedidos.db`

## Documentacao
- `docs/DescricaoProblema.md`
- `docs/DCU.md`
- `docs/DER.md`
- `docs/ArquiteturaProposta.md`
- `docs/DocumentacaoCompleta.md`
- `docs/README.md`

## Como executar
Use a mesma versao de `java` e `javac`. O projeto atual foi compilado com Java moderno e nao roda em Java 8.

1. Compilar:
```powershell
javac Main\App.java
```
2. Rodar:
```powershell
java Main.App
```
3. Abrir no navegador:
`http://localhost:18080`
