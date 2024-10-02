# Diff Generator CLI

![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)

## Descrição

O Diff Generator CLI é uma ferramenta de linha de comando desenvolvida em Java com Spring Boot que permite gerar arquivos de diferença (diff) entre dois arquivos. A ferramenta recebe três argumentos de entrada obrigatórios: o caminho do arquivo de origem, o caminho do arquivo de destino e o caminho para o arquivo de saída. Além disso, também há um argumento opcional para definir um tempo limite (timeout) para a operação.

## Funcionalidades
 Gera um arquivo de diff entre um arquivo fonte e um arquivo alvo.

## Estrutura de Pastas
O projeto possui a seguinte estrutura de pastas dentro do diretório src:

```bash

src/
│
├── cli/
├── cli.core/
│   ├── bsdiff/
│   └── similarity/
└── cli.shared/

````

### Pré-requisitos

- Eclipse IDE com suporte a projetos Java.
- Java Development Kit (JDK) 17 ou superior instalado.
- Projeto configurado no Eclipse.

## Gerando o Arquivo JAR Standalone no Eclipse

Para gerar um arquivo JAR standalone que pode ser executado via linha de comando, siga os passos abaixo usando o Eclipse:

### Passos para Gerar o JAR Executável

1. **Abra o projeto no Eclipse**:
   - Inicie o Eclipse e importe o projeto `Diff Generator CLI` caso ele ainda não esteja aberto.

2. **Compilar o projeto**:
   - Verifique se o projeto está compilado corretamente. Se houver erros, corrija-os antes de continuar.
   - Para compilar, clique com o botão direito no projeto e selecione `Project > Build Project`.

3. **Criar o arquivo JAR**:
   - Clique com o botão direito sobre o projeto na aba `Package Explorer` e selecione `Export`.
   - No menu que abrir, escolha `Java > Runnable JAR file` e clique em `Next`.

4. **Configurar a Exportação do JAR**:
   - Na seção `Launch configuration`, selecione a classe principal do projeto que contém o método `main`. No seu caso, escolha a classe `br.com.stone.tms.bsdiff.cli.Application`.
   - Em `Export destination`, escolha o local onde o arquivo JAR será salvo, por exemplo: `/caminho/para/gerar/diff-generator-cli.jar`.
   - Marque a opção `Package required libraries into generated JAR` para garantir que todas as dependências sejam incluídas no JAR standalone.

5. **Finalizar a exportação**:
   - Clique em `Finish` para gerar o arquivo JAR.
   - Se aparecer uma mensagem informando que o arquivo já existe, confirme para sobrescrevê-lo.

## Executando o Projeto via CLI

O Diff Generator CLI é executado via linha de comando e requer três argumentos obrigatórios: o arquivo de origem (`--source`), o arquivo de destino (`--target`) e o arquivo de saída (`--output`). Além disso, há um argumento opcional para definir um tempo limite para a operação (`--timeout`), que especifica quantos segundos a geração do diff pode levar antes de ser cancelada.

### Comando

```bash
java -jar target/diff-generator-cli.jar --source <caminho-do-arquivo-fonte> --target <caminho-do-arquivo-alvo> --output <caminho-do-arquivo-de-saida>
