# Loja Online - TP

Aplicacao Java da disciplina implementada com `MVC + DAO`, persistencia em arquivos binarios, indices primarios em disco e relacionamento `Cliente 1:N Pedido` usando hash extensivel. A interface obrigatoria foi entregue em front-end web local com `HttpServer`.

## Estrutura
- `Model/`: entidades de dominio e interface `Registro`.
- `DAO/`: acesso a dados, indice primario em hash extensivel e indice do relacionamento `1:N`.
- `Controller/`: validacoes, regras de negocio e integridade referencial.
- `View/`: HTML/CSS da interface web.
- `Main/`: servidor HTTP e rotas.
- `Util/`: serializacao binaria de strings e blocos multivalorados.
- `docs/`: documentacao tecnica e respostas do formulario.
- `data/`: arquivos de dados e indices persistidos entre execucoes.

## Entregas da Fase II atendidas
- CRUD completo para `Cliente`, `Produto`, `Cupom` e `Pedido`.
- Indice primario persistente para todas as tabelas.
- Relacionamento `Cliente 1:N Pedido` implementado com hash extensivel.
- Exclusao logica por lapide.
- Front-end web para todas as operacoes principais.
- Validacao de entradas e mensagens de erro para casos comuns.

## Persistencia em disco
Arquivos de dados:
- `data/clientes.db`
- `data/produtos.db`
- `data/cupons.db`
- `data/pedidos.db`

Cada `*.db` armazena:
- cabecalho `int` com o ultimo ID;
- lapide `boolean`;
- tamanho do registro `int`;
- payload binario da entidade.

Arquivos de indice primario gerados automaticamente:
- `*.pk.dir.db`: diretorio do hash extensivel.
- `*.pk.buckets.db`: buckets do hash extensivel com pares `PK -> posicao no arquivo de dados`.

Arquivos do relacionamento `Cliente -> Pedidos`:
- `data/pedidos.db.cliente_pedidos.dir.db`
- `data/pedidos.db.cliente_pedidos.buckets.db`
- `data/pedidos.db.cliente_pedidos.list.db`

## Como compilar e executar
O ambiente deste workspace possui `java` em versao antiga, entao a forma mais segura e compilar com compatibilidade Java 8:

1. Compilar:
```powershell
javac --release 8 Main\App.java
```

2. Executar:
```powershell
java Main.App
```

3. Abrir no navegador:
`http://localhost:18080`

## Funcionalidades da interface
- `/clientes`: CRUD e listagem de clientes.
- `/produtos`: CRUD e listagem de produtos.
- `/cupons`: CRUD e listagem de cupons.
- `/pedidos`: CRUD, associacao de cupom e consulta do relacionamento `1:N` por cliente.

## Documentacao
- `docs/DocumentacaoCompleta.md`
- `docs/ArquiteturaProposta.md`
- `docs/DER.md`
- `docs/DCU.md`
