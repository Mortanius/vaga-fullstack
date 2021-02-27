<img width="150" src="https://i.ibb.co/m0xnbYR/logo-nome.png">

# Avaliação prática - Fullstack Web Varejo 360

Esta avaliação tem por objetivo avaliar sua capacidade de desenvolver um sistema do zero, utilizando um conjunto de tecnologias pré-definidas.

## Descrição do sistema

Você implementará o sistema **CATÁLOGO DE LEITE**.

Nesse sistema, o usuário pode buscar os produtos informando:
- o início do código do produto ou
- parte do nome do produto

Após a busca, o sistema apresenta em uma tabela todos os produtos que atenderam ao filtro. Essa tabela deve exibir 10 itens por página.

Dentro da tabela, deve haver uma coluna com um botão para a exclusão daquele item.

O usuário também pode cadastrar um novo produto, informando o código e o nome. Para o cadastro, é essencial garantir que o código do produto é único.

Toda vez que um cadastro é concluído com sucesso, a tabela de resultado da busca deve ser atualizada.

Abaixo, temos um mock desse sistema e um vídeo explicando como funciona a interação do usuário com o sistema.

**Vídeo explicativo:** https://share.vidyard.com/watch/4kqLwuEqbA4QcB7Bnud3wP

**Mock:** https://www.figma.com/proto/3EBOfV1Pcby3fsWqQppiwk/Cat%C3%A1logo-do-Leite?node-id=20%3A109&scaling=min-zoom

## Requisitos técnicos

O sistema deve ser um website com uma API REST.

O frontend deve ser implementado em Angular 11 ou superior. 

A API REST (backend) deve ser implementado em Kotlin ou Java, utilizando Spring Boot 2.4.2 ou superior. 

O sistema deverá se conectar a um banco de dados PostgreSQL, cujas configurações de acesso foram enviadas em seu email.


## Requisitos extra

Segue abaixo uma lista de requisitos extra do sistema. Eles são pontos que, apesar de serem extras, tem grande importância na avaliação.

1. Implementar a ordenação na tabela. O usuário pode escolher a ordenação clicando no cabeçalho da tabela
2. Permitir que o usuário edite a descrição de um produto
3. Criar um conjunto de testes de controller (3 casos de teste) utilizando MockMVC
4. Criar Dockerfile para construir a imagem do servidor e adicionar as instruções de build e run deste Dockerfile no README


## Instruções

Para a implementação do sistema, crie um fork desse repositório e adicione todo o código nesse fork. Ao fim, gere um PR neste repositório com o título `fullstack_web - <nome completo>` para que seja avaliado.


## Avaliação

Além do funcionamento do sistema, será avaliada a organização do código, comentários e a identidade visual do sistema.


## Instruções para construção (build) e execução (run) do frontend

- Adicionar o endereço base da API no arquivo *environment.ts* na pasta *environments*
### Executando
````
ng serve
````

### Criando docker image
````
docker build -t catalogodeleiteapp .
````
### Criando e executando container
````
docker run -it -e PORT=<PORTA_COTAINER> -p <PORTA_HOST>:<PORTA_CONTAINER> catalogodeleiteapp
````

## Instruções para construção (build) e execução (run) do backend

- Preencher as informações no arquivo *application.yml*, na pasta *resources*
  - Dados de conexão com o banco de dados PostgreSQL
  - Em qual porta executar
- Adicionar o endereço do frontend à annotation @CrossOrigin.
- Executar o arquivo gradlew com a tarefa (Task) desejada

### Executando
````
./gradlew bootRun
````

### Criando um arquivo *jar*
````
./gradlew bootJar
````

### Criando docker image
````
./gradlew bootBuildImage
````
ou
````
docker build -t catalogodeleite .
````
### Criando e executando container
````
docker run -it -e PORT=<PORTA_COTAINER> -p <PORTA_HOST>:<PORTA_CONTAINER> catalogodeleite
````