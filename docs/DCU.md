# Diagrama de Caso de Uso (DCU)

## Atores
- Cliente: realiza pedidos.
- Administrador: gerencia cadastros, consultas, atualizacoes e exclusoes logicas.

## Casos de Uso Implementados
- RF01: Cadastrar Cliente.
- RF02: Cadastrar Produto.
- RF03: Criar Pedido (com multiplos produtos).
- RF04: Associar Cupom a Pedido.
- RF05: Listar registros ativos.
- RF06: Excluir registros (lapide).
- RF07: Atualizar registros existentes.
- RF08: Consultar registro por identificador.

## Fonte PlantUML (DCU)
```plantuml
@startuml
left to right direction
actor Cliente
actor Administrador

rectangle Sistema {
  usecase "Cadastrar Cliente" as UC1
  usecase "Cadastrar Produto" as UC2
  usecase "Criar Pedido" as UC3
  usecase "Associar Cupom ao Pedido" as UC4
  usecase "Listar Registros Ativos" as UC5
  usecase "Excluir Registro (Lapide)" as UC6
  usecase "Atualizar Registro" as UC7
  usecase "Consultar por ID" as UC8
}

Cliente --> UC3
Administrador --> UC1
Administrador --> UC2
Administrador --> UC4
Administrador --> UC5
Administrador --> UC6
Administrador --> UC7
Administrador --> UC8
@enduml
```
