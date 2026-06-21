# Loja Online - TP - Fase V

Aplicacao Java com `MVC + DAO`, persistencia binaria, interface web local, backup compactado dos arquivos de dados, casamento de padroes e criptografia XOR. A Fase V preserva CRUD, indices, relacionamentos e compressao das fases anteriores e acrescenta pesquisa textual com KMP/Boyer-Moore e criptografia de campo sensivel.

Repositorio GitHub: <https://github.com/lucaszanfa/Tp-Aedes3>

Video explicativo da Fase V: <https://youtu.be/dXa_AAKLjUk>

## Estrutura
- `Model/`: entidades de dominio e interface `Registro`.
- `DAO/`: acesso a dados, hash extensivel, Arvore B+ e tabela associativa.
- `Controller/`: validacoes, regras de negocio e integridade referencial.
- `View/`: HTML/CSS da interface web.
- `Main/`: servidor HTTP e rotas.
- `Util/`: serializacao binaria, casamento de padroes, criptografia e compressao.
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
- Pesquisa por padrao no campo textual `Produto.nome`.
- Implementacao completa de KMP (Knuth-Morris-Pratt).
- Implementacao completa de Boyer-Moore com heuristica bad character.
- Interface web em `/pesquisa` com escolha do algoritmo e exibicao dos registros encontrados.
- Criptografia XOR aplicada ao campo sensivel `Cliente.email`.

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

## Casamento de padroes e criptografia
Os algoritmos de busca estao em `Util/PatternMatcher.java`:
- `containsKmp(text, pattern)`: usa a tabela LPS para evitar retrocesso no texto.
- `containsBoyerMoore(text, pattern)`: usa a tabela bad character para deslocar o padrao quando ocorre divergencia.

A integracao com a base esta em `Controller/PesquisaController.java`. A pesquisa percorre os produtos ativos e aplica o algoritmo escolhido sobre `Produto.nome`, ignorando diferencas entre maiusculas e minusculas.

A interface esta disponivel em:

```text
http://localhost:18080/pesquisa
```

O campo criptografado e `Cliente.email`. Na serializacao de `Model/Cliente.java`, o email e salvo com `XorCipher.encrypt(email)`; na leitura, o sistema chama `XorCipher.decryptIfEncrypted(...)`. A classe `Util/XorCipher.java` aplica XOR byte a byte com chave fixa do projeto e grava o resultado em Base64 com prefixo `XOR1:`.

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
- `/pesquisa`: busca textual por KMP ou Boyer-Moore no nome dos produtos.
- `/compressao`: backup unico dos dados com Huffman e LZW.

## Documentacao
- `docs/RelatorioFaseIII.md`
- `docs/RelatorioFaseIV.md`
- `docs/RelatorioFaseV.md`
- `docs/RelatorioFaseIII.pdf`
- `docs/ArquiteturaProposta.md`
- `docs/DER.md`
- `docs/DCU.md`
