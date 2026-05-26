# Loja Online - TP - Fase III

Aplicacao Java com `MVC + DAO`, persistencia binaria e interface web local. A Fase III implementa o relacionamento `Pedido N:N Produto` por meio da tabela associativa `PedidoProduto`, com chave primaria composta e indices B+ persistentes para navegacao e consulta ordenada.

## Estrutura
- `Model/`: entidades de dominio e interface `Registro`.
- `DAO/`: acesso a dados, hash extensivel, Arvore B+ e tabela associativa.
- `Controller/`: validacoes, regras de negocio e integridade referencial.
- `View/`: HTML/CSS da interface web.
- `Main/`: servidor HTTP e rotas.
- `Util/`: serializacao binaria de strings e blocos multivalorados.
- `docs/`: documentacao tecnica e respostas do formulario.
- `data/`: arquivos de dados e indices persistidos entre execucoes.

## Entregas da Fase III atendidas
- CRUD completo para `Cliente`, `Produto`, `Cupom` e `Pedido`.
- Indice primario persistente para todas as tabelas.
- Relacionamento `Pedido N:N Produto` implementado por `PedidoProduto`.
- Chave composta `(idPedido, idProduto)`, sem identificador artificial.
- Consulta `Pedido -> Produtos` e `Produto -> Pedidos` na pagina web de pedidos.
- Arvore B+ persistente aplicada a listagem ordenada de produtos por ID.
- Hash extensivel mantido para buscas pontuais por PK e para `Cliente -> Pedidos`.
- Exclusao logica por lapide.
- Front-end web para todas as operacoes principais.
- Validacao de entradas e mensagens de erro para casos comuns.

## Persistencia em disco
Arquivos de dados:
- `data/clientes.db`
- `data/produtos.db`
- `data/cupons.db`
- `data/pedidos.db`
- `data/pedido_produto.db`

Cada arquivo de dados armazena:
- cabecalho `int` de controle (ultimo ID nas entidades ou total de insercoes na associativa);
- lapide `boolean`;
- tamanho do registro `int`;
- payload binario da entidade.

Arquivos de indice primario gerados automaticamente:
- `*.pk.dir.db`: diretorio do hash extensivel.
- `*.pk.buckets.db`: buckets do hash extensivel com pares `PK -> posicao no arquivo de dados`.

Arquivos do relacionamento `Cliente -> Pedidos`:
- `data/pedidos.db.cliente_pedidos.dir.db`
- `data/pedidos.db.cliente_pedidos.buckets.db`

Arquivos da Fase III:
- `data/pedido_produto.db`: registros associativos com lapide.
- `data/pedido_produto.db.pedido.bplus.db`: B+ pela chave `(idPedido, idProduto)`.
- `data/pedido_produto.db.produto.bplus.db`: B+ reversa `(idProduto, idPedido)`.
- `data/produtos.db.ordem_id.bplus.db`: B+ da consulta ordenada do catalogo.
- `data/pedidos.db.cliente_pedidos.list.db`

## Como compilar e executar
O runtime disponivel e Java 8; compile para uma pasta de build com compatibilidade Java 8:
```powershell
New-Item -ItemType Directory -Force out\classes | Out-Null
$sources = Get-ChildItem -Recurse -Filter *.java -Path Controller,DAO,Main,Model,Util,View | ForEach-Object { $_.FullName }
javac --release 8 -d out\classes $sources
```

Executar:
```powershell
java -cp out\classes Main.App
```

Abrir no navegador:
`http://localhost:18080`

## Funcionalidades da interface
- `/clientes`: CRUD e listagem de clientes.
- `/produtos`: CRUD e listagem ordenada pela Arvore B+.
- `/cupons`: CRUD e listagem de cupons.
- `/pedidos`: CRUD, cupom, consulta `Cliente -> Pedidos` e navegacao N:N nos dois sentidos.

## Documentacao
- `docs/RelatorioFaseIII.md`
- `docs/RelatorioFaseIII.pdf`
- `docs/ArquiteturaProposta.md`
- `docs/DER.md`
- `docs/DCU.md`
