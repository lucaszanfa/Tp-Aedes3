# Sistema de Pedidos - TP

Implementacao completa em Java seguindo MVC + DAO com persistencia em arquivos binarios com cabecalho e exclusao logica por lapide.

## Estrutura
- `Model/`: entidades de dominio e interface `Registro`.
- `DAO/`: acesso a dados em arquivo binario.
- `Controller/`: regras de negocio.
- `View/`: composicao HTML/CSS.
- `Main/`: inicializacao do servidor HTTP.
- `Util/`: utilitarios de serializacao.
- `docs/`: documentacao (DCU, DER, arquitetura proposta textual).
- `data/`: arquivos binarios gerados em runtime.

## Requisitos Atendidos
- RF01 a RF08: CRUD, consulta, listagem de ativos, exclusao logica, pedido com multiplos produtos e associacao de cupom.
- RNF01: sem interface por console para uso do sistema.
- RNF02: interface minima HTML/CSS.
- RNF03: persistencia binaria com cabecalho e lapide.
- RNF04: documentacao entregue.

## Regra Extra de Strings
Foi implementado em `Util/BinaryStringIO.java`:
- `2 bytes`: quantidade de strings do bloco.
- para cada string:
  - `4 bytes`: tamanho da string em bytes UTF-8;
  - `N bytes`: conteudo da string.

## Como executar
1. Compilar:
```powershell
javac Main\App.java
```
2. Rodar:
```powershell
java Main.App
```
3. Abrir no navegador:
`http://localhost:8080`
