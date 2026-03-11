# Descricao do Problema

## 1. Tema do projeto
O projeto foi desenvolvido com o tema **Loja Online**. A proposta representa um sistema web simples para gerenciamento de clientes, produtos, pedidos e cupons promocionais, com interface HTML/CSS e persistencia em arquivos binarios.

## 2. Descricao do problema
Uma loja online precisa manter o controle de seus clientes cadastrados, dos produtos disponiveis para venda e dos pedidos realizados. Alem disso, o sistema deve permitir o uso de cupons de desconto associados aos pedidos.

Neste projeto, os dados nao sao armazenados em banco de dados relacional. Em vez disso, a persistencia e feita em **arquivos binarios**, cada um com **cabecalho de controle de ultimo identificador** e com **exclusao logica por lapide**, conforme exigido no enunciado do trabalho.

O sistema foi implementado em Java seguindo o padrao **MVC + DAO**, com interface web minima servida localmente por HTTP.

## 3. Objetivo do trabalho
- Desenvolver um sistema que permita o CRUD de clientes e produtos.
- Permitir a criacao e consulta de pedidos com multiplos produtos.
- Permitir a associacao de cupons a pedidos existentes.
- Garantir persistencia em arquivos binarios com cabecalho.
- Implementar exclusao logica utilizando lapide.
- Organizar a documentacao com DCU, DER e Arquitetura Proposta.

## 4. Requisitos funcionais
- **RF01**: Cadastrar cliente.
- **RF02**: Cadastrar produto.
- **RF03**: Criar pedido com multiplos produtos.
- **RF04**: Associar cupom a pedido.
- **RF05**: Listar registros ativos.
- **RF06**: Excluir registros com logica de lapide.
- **RF07**: Atualizar registros existentes.
- **RF08**: Consultar registro por identificador.

## 5. Requisitos nao funcionais
- **RNF01**: O sistema nao utiliza console como interface principal de uso.
- **RNF02**: A interface foi implementada em HTML/CSS.
- **RNF03**: A persistencia e feita obrigatoriamente em arquivos binarios com cabecalho.
- **RNF04**: A documentacao do projeto foi organizada na pasta `docs`.

## 6. Atores
- **Cliente**: ator de negocio que realiza pedidos no sistema.
- **Administrador**: responsavel por gerenciar cadastros, consultas, atualizacoes e exclusoes logicas.

## 7. Correspondencia com o projeto implementado
No projeto desenvolvido:
- `Cliente` possui `id`, `nome`, `email` e `telefone`.
- `Produto` possui `id`, `nome`, `preco` e `estoque`.
- `Pedido` possui `idCliente`, `idCupom`, `valorTotal`, lista de IDs de produtos e lista de quantidades.
- `Cupom` possui `codigo`, `percentualDesconto` e status `ativo`.
- A interface e acessada pelo navegador em `http://localhost:8080`.
- Os dados sao armazenados em `data/clientes.db`, `data/produtos.db`, `data/pedidos.db` e `data/cupons.db`.
