# Loja Online - Relatorio Tecnico da Fase V

## 1. Link para o GitHub

Repositorio do projeto:

<https://github.com/lucaszanfa/Tp-Aedes3>

Video explicativo da Fase V:

<https://youtu.be/dXa_AAKLjUk>

## 2. Atualizacao do README

O arquivo `README.md` foi atualizado com:

- link do repositorio;
- instrucoes de compilacao e execucao;
- descricao da rota `/pesquisa`;
- descricao dos algoritmos KMP e Boyer-Moore;
- descricao da criptografia XOR aplicada ao campo `Cliente.email`.

## 3. Objetivo da fase

Esta fase acrescenta dois recursos ao aplicativo desenvolvido nas etapas
anteriores:

- casamento de padroes com KMP e Boyer-Moore;
- criptografia XOR em um campo considerado sensivel.

Os recursos foram integrados ao sistema web local, preservando a arquitetura
`MVC + DAO`, os arquivos binarios, os indices, a compressao e os CRUDs ja
existentes.

## 4. Arquivos principais

| Arquivo | Responsabilidade |
| --- | --- |
| `Util/PatternMatcher.java` | Implementa KMP e Boyer-Moore. |
| `Controller/PesquisaController.java` | Aplica o algoritmo escolhido sobre os produtos. |
| `Main/App.java` | Cria a rota `/pesquisa`, recebe os dados do formulario e exibe os resultados. |
| `View/HtmlView.java` | Inclui a opcao de menu "Pesquisar por padrao (KMP / BM)". |
| `Util/XorCipher.java` | Implementa a criptografia e descriptografia XOR. |
| `Model/Cliente.java` | Criptografa o email ao salvar e descriptografa ao ler. |

## 5. Interface de pesquisa

A interface fica disponivel em:

```text
http://localhost:18080/pesquisa
```

Ela atende aos requisitos porque:

- existe uma opcao no menu chamada "Pesquisar por padrao (KMP / BM)";
- o usuario escolhe o algoritmo em um `select`;
- o usuario informa o padrao em um campo de texto;
- o sistema retorna uma tabela com os produtos encontrados.

O trecho de integracao na rota `/pesquisa` recebe os dados do formulario e
chama o controller:

```java
String algoritmo = pesquisaController.normalizeAlgorithm(data.get("algoritmo"));
String padrao = data.get("padrao");
List<Produto> encontrados = pesquisaController.pesquisarProdutosPorNome(algoritmo, padrao);
```

## 6. Formulario tecnico

### 1. Qual campo textual foi escolhido para aplicar os algoritmos de casamento de padroes? Por que?

O campo escolhido foi `nome` da entidade `Produto`.

Esse campo foi escolhido porque e textual, aparece naturalmente no catalogo da
loja e representa uma consulta real para o usuario. Em um sistema de loja
online, buscar produtos por parte do nome e uma funcionalidade comum. Assim,
faz sentido aplicar KMP e Boyer-Moore sobre `Produto.nome` em vez de campos
numericos como ID, preco ou estoque.

### 2. Explique o funcionamento do KMP implementado

O KMP esta implementado no metodo `containsKmp` da classe
`Util.PatternMatcher`.

Primeiro, o algoritmo monta a tabela LPS (`Longest Prefix Suffix`) por meio do
metodo `buildLongestPrefixSuffix`. Essa tabela armazena, para cada posicao do
padrao, o tamanho do maior prefixo que tambem e sufixo ate aquela posicao.

Depois, o metodo percorre o texto com o indice `i` e o padrao com o indice
`j`. Quando os caracteres sao iguais, os dois indices avancam. Quando o indice
`j` chega ao tamanho do padrao, significa que o padrao foi encontrado.

Quando ocorre uma diferenca, o KMP nao volta no texto. Se `j > 0`, ele usa a
tabela LPS para reposicionar `j`:

```java
j = lps[j - 1];
```

Se `j == 0`, apenas o indice do texto avanca. Com isso, o algoritmo evita
comparacoes repetidas e faz a busca de forma linear em relacao ao tamanho do
texto e do padrao.

### 3. Explique o funcionamento do Boyer-Moore implementado

O Boyer-Moore esta implementado no metodo `containsBoyerMoore` da classe
`Util.PatternMatcher`.

A versao implementada usa a heuristica obrigatoria de bad character. Primeiro,
o metodo `buildBadCharacterTable` cria uma tabela com a ultima posicao em que
cada caractere aparece no padrao. Caracteres ausentes recebem valor `-1`.

Durante a busca, o algoritmo alinha o padrao sobre o texto e compara da direita
para a esquerda. Se todos os caracteres forem iguais, o padrao foi encontrado.
Se houver divergencia, o deslocamento e calculado com base na ultima ocorrencia
do caractere ruim:

```java
shift += Math.max(1, j - badChar[text.charAt(shift + j)]);
```

Esse calculo permite saltar mais de uma posicao quando possivel, em vez de
avancar sempre caractere por caractere. A heuristica good suffix nao foi usada,
pois era opcional no enunciado.

### 4. Descreva como integrou os algoritmos ao sistema.

A integracao foi feita em tres camadas:

1. Na camada utilitaria, `Util.PatternMatcher` concentra os algoritmos KMP e
Boyer-Moore.
2. Na camada de controle, `Controller.PesquisaController` recebe o algoritmo e
o padrao, percorre os produtos retornados pelo DAO e aplica o algoritmo sobre
`Produto.nome`.
3. Na camada web, `Main.App` criou a rota `/pesquisa`, com formulario para
escolher o algoritmo e informar o padrao. O menu foi atualizado em
`View.HtmlView`.

No controller, a escolha do algoritmo ocorre neste ponto:

```java
boolean match = "BM".equals(normalizedAlgorithm)
    ? PatternMatcher.containsBoyerMoore(nome, normalizedPattern)
    : PatternMatcher.containsKmp(nome, normalizedPattern);
```

Quando ha correspondencia, o produto e adicionado a lista de encontrados e
depois exibido em uma tabela HTML.

### 5. Quais dificuldades encontrou na implementacao dos dois algoritmos?

A principal dificuldade no KMP foi montar corretamente a tabela LPS, pois ela
precisa tratar casos em que ha prefixos e sufixos parciais no padrao. Para
resolver isso, a construcao da tabela foi separada em um metodo proprio,
mantendo a logica de busca mais simples.

No Boyer-Moore, o cuidado principal foi implementar corretamente a tabela bad
character e garantir que o deslocamento nunca fosse zero ou negativo. Por isso,
o deslocamento usa `Math.max(1, ...)`.

Outra dificuldade foi integrar os algoritmos ao sistema sem quebrar o padrao
MVC. A solucao foi deixar os algoritmos em `Util`, a regra de pesquisa em
`Controller` e a interface em `Main/App.java` e `View/HtmlView.java`.

### 6. Qual campo foi utilizado na criptografia?

O campo utilizado na criptografia foi `email` da entidade `Cliente`.

Ele foi escolhido porque e um dado pessoal e sensivel do cliente. Dessa forma,
o arquivo binario nao armazena o email diretamente em texto puro.

### 7. Qual foi o metodo utilizado na criptografia?

Foi usada criptografia XOR, implementada em `Util.XorCipher`.

O metodo converte o texto para bytes em UTF-8 e aplica o operador XOR (`^`)
entre cada byte do texto e um byte da chave. Quando a chave chega ao fim, ela
e reutilizada de forma ciclica:

```java
output[i] = (byte) (input[i] ^ KEY[i % KEY.length]);
```

O resultado criptografado e convertido para Base64 e recebe o prefixo `XOR1:`.
Esse prefixo permite identificar se o valor lido do arquivo ja esta
criptografado.

Na gravacao do cliente:

```java
values[1] = XorCipher.encrypt(email);
```

Na leitura do cliente:

```java
email = values.length > 1 ? XorCipher.decryptIfEncrypted(values[1]) : "";
```

Assim, o dado fica protegido no arquivo, mas continua aparecendo normalmente
para o usuario depois da leitura.

## 7. Como compilar e executar

Compilar:

```powershell
New-Item -ItemType Directory -Force out\classes | Out-Null
$sources = Get-ChildItem -Recurse -Filter *.java -Path Controller,DAO,Main,Model,Util,View | ForEach-Object { $_.FullName }
javac --release 8 -d out\classes $sources
```

Executar:

```powershell
java -cp out\classes Main.App
```

Abrir:

```text
http://localhost:18080
```

Tela de pesquisa:

```text
http://localhost:18080/pesquisa
```

## 8. Video explicativo

Video da Fase V:

<https://youtu.be/dXa_AAKLjUk>

O video explicativo mostra principalmente:

- `Util/PatternMatcher.java`;
- `Controller/PesquisaController.java`;
- rota `/pesquisa` em `Main/App.java`;
- `Util/XorCipher.java`;
- serializacao e leitura do email em `Model/Cliente.java`.

## 9. Conclusao

A fase foi integrada ao aplicativo com os dois algoritmos de casamento de
padroes pedidos e com criptografia XOR em campo sensivel. A busca usa o campo
`Produto.nome`, a interface permite escolher entre KMP e Boyer-Moore, e os
registros encontrados sao retornados ao usuario pela tela web. A criptografia
foi aplicada ao email do cliente durante a persistencia binaria, com
descriptografia automatica na leitura.
