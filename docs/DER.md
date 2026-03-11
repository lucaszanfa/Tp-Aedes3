# Diagrama Entidade-Relacionamento (DER)

## Entidades
- Cliente(id, nome, email, telefone)
- Produto(id, nome, preco, estoque)
- Cupom(id, codigo, percentualDesconto, ativo)
- Pedido(id, idCliente, idCupom, valorTotal)
- ItemPedido(idPedido, idProduto, quantidade)

## Relacionamentos
- Cliente 1:N Pedido
- Pedido N:N Produto (resolvido por ItemPedido)
- Pedido 0:1 Cupom

## Regras de Integridade
- Um pedido precisa de um cliente valido.
- Um pedido deve possuir ao menos um item.
- Quantidade de item deve ser > 0.
- Estoque do produto e baixado na criacao do pedido.
- Cupom so pode ser associado se estiver ativo.

## Fonte Mermaid (DER)
```mermaid
erDiagram
    CLIENTE ||--o{ PEDIDO : realiza
    PEDIDO ||--o{ ITEM_PEDIDO : contem
    PRODUTO ||--o{ ITEM_PEDIDO : compoe
    CUPOM o|--o{ PEDIDO : "aplicado em"

    CLIENTE {
        int id PK
        string nome
        string email
        string telefone
    }

    PRODUTO {
        int id PK
        string nome
        double preco
        int estoque
    }

    CUPOM {
        int id PK
        string codigo
        double percentualDesconto
        boolean ativo
    }

    PEDIDO {
        int id PK
        int idCliente FK
        int idCupom FK
        double valorTotal
    }

    ITEM_PEDIDO {
        int idPedido FK
        int idProduto FK
        int quantidade
    }
```
