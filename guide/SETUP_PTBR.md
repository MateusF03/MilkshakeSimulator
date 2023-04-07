# Como fazer setup do bot

O único pré-requisito é você ter o ImageMagick instalado no seu PC. O link do programa está no README do projeto. Também será necessário um arquivo jar do bot, você pode conseguir compilando o projeto ou baixando o jar já compilado na aba de "releases".

## Após baixar o arquivo jar

Primeiramente você vai ter que ligar ele, você pode fazer isto usando o terminal do seu computador e digitando `java -jar (nome do arquivo)`. Na primeira execução do programa ele vai criar 2 arquivos, o `vips.txt` e o `.env` (caso você não copie o do repositório).

## Preenchendo os arquivos

Se o `.env` estiver vazio, você terá que preenchê-lo com `MILKSHAKE_TOKEN=<token>`, trocando `<token>` pelo token do seu bot. Para conseguir esse você vai ter que ir na [aba de aplicações do Discord](https://discord.com/developers/applications/) e [criar um bot novo](https://imgur.com/a/mwQLIJU).

Você também pode trocar o prefixo padrão adicionando uma linha com `MILKSHAKE_PREFIX=<prefix>` ao arquivo `.env`, trocando `<prefix>` pelo prefixo desejado (o padrão é `m!`).

Apesar de opcional, é recomendado preencher o `vips.txt`, colocando o ID de todo mundo que vai ter as permissões de usar os comandos especiais do bot.

## Após ligar o bot

Você pode receber um guia de como criar templates usando `prefixo + guide` (e.g. `m!guide`). Para saber de todos os comandos, use o comando de help, e para criar uma source com imagem é só usar o comando `source` e sem imagem com o comando `add`. 
