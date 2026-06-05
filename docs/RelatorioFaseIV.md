# Loja Online - Relatorio Tecnico da Fase IV

## 1. Objetivo

A Fase IV acrescenta compressao aos arquivos de dados do sistema sem alterar
o funcionamento das fases anteriores. O CRUD, os indices persistentes, a
exclusao logica e o relacionamento `Pedido N:N Produto` continuam operando
sobre os mesmos arquivos binarios.

A compactacao foi implementada no nivel de arquivo: o sistema primeiro
empacota todos os arquivos `.db` usados pelo aplicativo em um unico fluxo
binario e depois aplica o algoritmo de compressao sobre esse pacote completo.

## 2. Arquivos compactados

Foram incluidos todos os arquivos `.db` de `data/`, abrangendo bases
principais e indices:

- `clientes.db` e seus indices de hash extensivel.
- `produtos.db`, indice primario e indice B+ de ordenacao.
- `cupons.db` e seus indices.
- `pedidos.db`, indice primario e indices do relacionamento cliente-pedidos.
- `pedido_produto.db` e os indices B+ por pedido e por produto.

O empacotamento e feito por `Util.DataArchive`, que grava:

```text
[assinatura TPDB1]
[quantidade de arquivos]
[tamanho do nome][nome relativo][tamanho do arquivo][bytes do arquivo]
...
```

Assim, cada algoritmo recebe exatamente o mesmo arquivo original logico.

## 3. Implementacao

### 3.1 Huffman

Classe: `Util.HuffmanCompressor`

O compressor calcula a frequencia de cada byte do pacote, monta a arvore de
Huffman com fila de prioridade e grava um cabecalho contendo o tamanho
original e as frequencias usadas para reconstruir a arvore. Em seguida, os
bytes sao substituidos por seus codigos binarios de tamanho variavel.

Tambem foi implementada a descompressao para validar o resultado. Apos gerar
o arquivo `.huff`, o sistema descompacta em memoria e compara byte a byte com
o pacote original.

Arquivo gerado:

```text
backups/fase4_huffman.huff
```

### 3.2 LZW

Classe: `Util.LZWCompressor`

O LZW inicia o dicionario com os 256 bytes possiveis. Durante a leitura do
pacote, sequencias repetidas sao adicionadas ao dicionario e substituidas por
codigos de 16 bits. O limite do dicionario foi definido em 65.536 entradas,
compatível com a escrita dos codigos em dois bytes.

Assim como no Huffman, a descompressao foi implementada e usada na verificacao
de integridade.

Arquivo gerado:

```text
backups/fase4_lzw.lzw
```

## 4. Como executar

Pelo navegador:

```text
http://localhost:18080/compressao
```

Pela linha de comando:

```powershell
New-Item -ItemType Directory -Force out\classes | Out-Null
$sources = Get-ChildItem -Recurse -Filter *.java -Path Controller,DAO,Main,Model,Util,View | ForEach-Object { $_.FullName }
javac --release 8 -d out\classes $sources
java -cp out\classes Util.BackupCli
```

## 5. Formulario tecnico

### 1. Qual foi a taxa de compressao obtida com o algoritmo de Huffman?

**a. Tamanho do arquivo original**

O pacote unico original, contendo os arquivos `.db`, ficou com:

```text
4461 bytes
```

**b. Tamanho do arquivo comprimido**

O arquivo Huffman gerado ficou com:

```text
2925 bytes
```

**c. Calculo da taxa**

```text
taxa = 1 - (tamanho_comprimido / tamanho_original)
taxa = 1 - (2925 / 4461)
taxa = 0,3443 = 34,43%
```

**d. Interpretacao do resultado**

O Huffman reduziu o pacote em **34,43%**. O resultado e positivo porque os
arquivos binarios possuem muitos bytes repetidos, especialmente zeros,
cabecalhos pequenos e estruturas de indice com campos vazios. A taxa nao e
maior porque o backup e pequeno e o Huffman precisa gravar metadados da
arvore/frequencias no proprio arquivo compactado.

### 2. Qual foi a taxa de compressao obtida com o algoritmo de LZW?

**a. Tamanho do arquivo original**

O mesmo pacote unico original tinha:

```text
4461 bytes
```

**b. Tamanho do arquivo comprimido**

O arquivo LZW gerado ficou com:

```text
2422 bytes
```

**c. Calculo da taxa**

```text
taxa = 1 - (tamanho_comprimido / tamanho_original)
taxa = 1 - (2422 / 4461)
taxa = 0,4571 = 45,71%
```

**d. Interpretacao do resultado**

O LZW reduziu o pacote em **45,71%**, ficando melhor que Huffman nesta base.
Isso ocorreu porque o pacote contem sequencias repetidas de bytes e padroes
estruturais dos arquivos `.db` e dos indices. O LZW aproveita bem repeticoes
de sequencias, enquanto o Huffman trabalha principalmente com a frequencia de
bytes individuais.

### 3. Quais dificuldades surgiram ao implementar Huffman e LZW e como voce resolveu?

A primeira dificuldade foi atender ao requisito de gerar um unico arquivo
compactado sem comprimir cada tabela separadamente. A solucao foi criar
`DataArchive`, um formato simples de empacotamento que junta todos os `.db`
em um unico fluxo antes da compressao.

No Huffman, o cuidado principal foi gravar metadados suficientes para
descompactar depois. Para resolver isso, o arquivo compactado armazena o
tamanho original e a tabela de frequencias dos bytes presentes, permitindo
reconstruir a mesma arvore na leitura.

No LZW, a maior atencao foi manter o dicionario limitado para que cada codigo
coubesse em 16 bits. O dicionario foi limitado a 65.536 entradas, e a
descompressao trata o caso especial classico em que o codigo recebido e igual
ao proximo indice do dicionario.

Por fim, para preservar a integridade, os dois algoritmos foram testados com
descompressao imediata e comparacao byte a byte com o pacote original. A rota
`/compressao` e o CLI `Util.BackupCli` exibem se essa verificacao passou.

## 6. Resultado da verificacao

Execucao realizada em 05/06/2026:

| Algoritmo | Original | Comprimido | Taxa | Integridade |
| --- | ---: | ---: | ---: | --- |
| Huffman | 4461 bytes | 2925 bytes | 34,43% | Verificado |
| LZW | 4461 bytes | 2422 bytes | 45,71% | Verificado |

## 7. Exemplo de compressao

Os arquivos `backups/fase4_huffman.huff` e `backups/fase4_lzw.lzw` ja sao
exemplos reais gerados pelo sistema. Eles nao sao legiveis diretamente em
editor de texto porque armazenam dados binarios compactados. O conteudo deles
corresponde ao pacote unico criado a partir dos arquivos `.db` da pasta
`data/`.

Exemplo real da execucao:

| Arquivo compactado | Algoritmo | Entrada usada | Tamanho final |
| --- | --- | --- | ---: |
| `backups/fase4_huffman.huff` | Huffman | Pacote unico com 19 arquivos `.db` | 2925 bytes |
| `backups/fase4_lzw.lzw` | LZW | Pacote unico com 19 arquivos `.db` | 2422 bytes |

Para visualizar a ideia dos algoritmos, considere a sequencia didatica:

```text
BANANA_BANANA
```

No Huffman, os simbolos mais frequentes recebem codigos menores. Nessa
sequencia, `A` e `N` aparecem mais vezes que `B` e `_`, entao tenderiam a
receber codigos binarios mais curtos. O ganho vem da troca de bytes fixos por
codigos de tamanho variavel.

No LZW, o compressor cria entradas para sequencias repetidas. Depois de ler
partes como `BA`, `AN`, `NA` e `ANA`, a segunda ocorrencia de `BANANA` pode
ser representada por codigos de dicionario, reduzindo a repeticao literal dos
bytes.

## 8. Conclusao

A Fase IV foi implementada como backup completo em arquivo unico. Os arquivos
de dados e indices continuam sendo usados normalmente pelo aplicativo, e a
compressao atua apenas sobre uma copia empacotada. Huffman e LZW foram
implementados sem bibliotecas externas, ambos com descompressao de validacao
para garantir que o pacote original pode ser recuperado integralmente.
