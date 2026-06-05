# Loja Online - TP - Fase IV

Aplicacao Java com `MVC + DAO`, persistencia binaria, interface web local e backup compactado dos arquivos de dados. A Fase IV preserva CRUD, indices e relacionamentos das fases anteriores e acrescenta compressao em arquivo unico usando Huffman e LZW.

## Estrutura
- `Model/`: entidades de dominio e interface `Registro`.
- `DAO/`: acesso a dados, hash extensivel, Arvore B+ e tabela associativa.
- `Controller/`: validacoes, regras de negocio e integridade referencial.
- `View/`: HTML/CSS da interface web.
- `Main/`: servidor HTTP e rotas.
- `Util/`: serializacao binaria de strings e blocos multivalorados.
- `docs/`: documentacao tecnica e respostas do formulario.
- `data/`: arquivos de dados e indices persistidos entre execucoes.
- `backups/`: arquivos compactados gerados pela Fase IV.

## Entregas atendidas
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
- Backup completo em arquivo unico com todos os arquivos `.db` usados pelo aplicativo.
- Compressao de arquivo usando Huffman.
- Compressao de arquivo usando LZW.
- Verificacao de integridade por descompressao e comparacao byte a byte.

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

## Compressao da Fase IV
O backup e feito em duas etapas:
1. `Util.DataArchive` empacota todos os arquivos `.db` de `data/` em um unico fluxo binario, preservando nome relativo, tamanho e conteudo.
2. `Util.HuffmanCompressor` ou `Util.LZWCompressor` comprime esse pacote completo, gerando um unico arquivo final.

Arquivos gerados:
- `backups/fase4_huffman.huff`
- `backups/fase4_lzw.lzw`

Tambem ha uma rota web em `/compressao` para gerar os backups e visualizar tamanho original, tamanho comprimido, calculo da taxa e verificacao de integridade.

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

Gerar os backups por linha de comando:
```powershell
java -cp out\classes Util.BackupCli
```

## Funcionalidades da interface
- `/clientes`: CRUD e listagem de clientes.
- `/produtos`: CRUD e listagem ordenada pela Arvore B+.
- `/cupons`: CRUD e listagem de cupons.
- `/pedidos`: CRUD, cupom, consulta `Cliente -> Pedidos` e navegacao N:N nos dois sentidos.
- `/compressao`: backup unico dos dados com Huffman e LZW.

## Documentacao
- `docs/RelatorioFaseIII.md`
- `docs/RelatorioFaseIV.md`
- `docs/RelatorioFaseIII.pdf`
- `docs/ArquiteturaProposta.md`
- `docs/DER.md`
- `docs/DCU.md`
