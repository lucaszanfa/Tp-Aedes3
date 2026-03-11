# Documentacao Completa - Loja Online

## 1. Descricao do problema
O sistema desenvolvido representa uma **Loja Online** capaz de cadastrar e gerenciar clientes, produtos, pedidos e cupons. O objetivo e controlar o fluxo basico de vendas, permitindo registrar clientes, manter um catalogo de produtos, criar pedidos com varios itens e aplicar cupons promocionais.

Os dados do sistema sao persistidos em **arquivos binarios com cabecalho**, sem uso de banco de dados relacional, e a exclusao de registros e feita por **lapide**, preservando os dados fisicamente no arquivo.

## 2. Objetivo do trabalho
- Desenvolver um sistema com operacoes de cadastro, consulta, atualizacao, listagem e exclusao logica.
- Persistir os dados em arquivos binarios com controle de IDs.
- Seguir o padrao MVC + DAO.
- Fornecer documentacao com DCU, DER e arquitetura proposta.

## 3. Requisitos funcionais
- **RF01**: Cadastrar Cliente.
- **RF02**: Cadastrar Produto.
- **RF03**: Criar Pedido com multiplos produtos.
- **RF04**: Associar Cupom a Pedido.
- **RF05**: Listar registros ativos.
- **RF06**: Excluir registros com lapide.
- **RF07**: Atualizar registros existentes.
- **RF08**: Consultar registro por identificador.

## 4. Requisitos nao funcionais
- **RNF01**: O sistema nao utiliza console como interface de uso.
- **RNF02**: A interface foi implementada em HTML/CSS.
- **RNF03**: A persistencia e obrigatoriamente binaria, com cabecalho.
- **RNF04**: A documentacao foi entregue na pasta `docs`.

## 5. Atores
- **Cliente**: realiza pedidos.
- **Administrador**: gerencia cadastros, consultas, atualizacoes e exclusoes logicas.

## 6. Codigo do DCU
```plantuml
@startuml
left to right direction

actor Cliente
actor Administrador

rectangle "Sistema Loja Online" {
  usecase "Cadastrar Cliente" as UC1
  usecase "Cadastrar Produto" as UC2
  usecase "Criar Pedido" as UC3
  usecase "Associar Cupom a Pedido" as UC4
  usecase "Listar Registros Ativos" as UC5
  usecase "Excluir Registro com Lapide" as UC6
  usecase "Atualizar Registro" as UC7
  usecase "Consultar por ID" as UC8
}

Cliente --> UC3
Administrador --> UC1
Administrador --> UC2
Administrador --> UC3
Administrador --> UC4
Administrador --> UC5
Administrador --> UC6
Administrador --> UC7
Administrador --> UC8
@enduml
```

## 7. Codigo do DER
```mermaid
erDiagram
    CLIENTE ||--o{ PEDIDO : realiza
    PEDIDO ||--|{ ITEM_PEDIDO : contem
    PRODUTO ||--o{ ITEM_PEDIDO : participa
    CUPOM o|--o{ PEDIDO : aplica_desconto

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

## 8. Arquitetura proposta
O sistema segue o padrao **MVC + DAO**:
- **Model**: classes `Cliente`, `Produto`, `Pedido`, `Cupom` e `Registro`.
- **DAO**: manipulacao de arquivos binarios com cabecalho e lapide.
- **Controller**: validacoes e regras de negocio.
- **View**: interface HTML/CSS.
- **Main**: servidor HTTP e roteamento das paginas.

## 9. Diagrama de arquitetura em camadas
```mermaid
flowchart TD
    U[Administrador / Cliente] --> V[View<br/>HTML + CSS]
    V --> A[Main.App<br/>Servidor HTTP e rotas]

    A --> CC[ClienteController]
    A --> PC[ProdutoController]
    A --> CuC[CupomController]
    A --> PeC[PedidoController]

    CC --> M1[Cliente]
    PC --> M2[Produto]
    CuC --> M3[Cupom]
    PeC --> M4[Pedido]

    CC --> D1[ClienteDAO]
    PC --> D2[ProdutoDAO]
    CuC --> D3[CupomDAO]
    PeC --> D4[PedidoDAO]

    D1 --> F1[(clientes.db)]
    D2 --> F2[(produtos.db)]
    D3 --> F3[(cupons.db)]
    D4 --> F4[(pedidos.db)]
```
