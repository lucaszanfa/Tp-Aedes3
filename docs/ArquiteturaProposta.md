# Arquitetura Proposta (sem diagrama grafico)

Conforme solicitado, este documento descreve a arquitetura em camadas (MVC + DAO), sem incluir o diagrama de arquitetura.

## Camadas
- `Model`: entidades de dominio (`Cliente`, `Produto`, `Pedido`, `Cupom`) e interface `Registro`.
- `DAO`: persistencia binaria em arquivo com cabecalho de ultimo ID e exclusao logica por lapide.
- `Controller`: regras de negocio e validacoes de CRUD, pedido e cupom.
- `View`: interface HTML/CSS minima servida por HTTP.
- `Main`: bootstrap da aplicacao e roteamento das paginas/acoes.

## Persistencia Binaria
- Cada arquivo de entidade inicia com cabecalho de 4 bytes (`int`) para controle de ultimo ID.
- Estrutura de registro:
  - `lapide` (1 byte): `false` ativo, `true` excluido logicamente.
  - `tamanho` (4 bytes): tamanho do payload.
  - `payload` (N bytes): bytes da entidade.
- Regra de string aplicada:
  - `2 bytes` (`short`) com quantidade de strings no bloco.
  - Para cada string: `4 bytes` (`int`) do tamanho e em seguida os bytes UTF-8.

## Fluxo de Operacoes
- Cadastro: Controller valida dados e chama DAO `create`.
- Consulta/listagem: Controller usa DAO `read` e `listActive`.
- Atualizacao: Controller valida e chama DAO `update` (in-place ou append com lapide).
- Exclusao: Controller chama DAO `delete` (marca lapide).
- Pedido:
  - valida cliente/produtos/estoque;
  - baixa estoque;
  - grava pedido;
  - permite associar cupom ativo posteriormente.
