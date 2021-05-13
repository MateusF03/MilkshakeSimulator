# Como fazer setup do bot

O único pré-requisito é você ter o ImageMagick instalado no seu PC. O link do programa está no README do projeto. Também será necessário um arquivo jar do bot, você pode conseguir compilando o projeto ou baixando o jar já compilado na aba de "releases".

## Após baixar o arquivo jar

Primeiramente você vai ter que ligar ele, você pode fazer isto usando o terminal do seu computador e digitando `java -jar (nome do arquivo)`. Na primeira execução do programa ele vai criar 2 arquivos, o `token.txt` e `vips.txt`.

## Preenchendo os arquivos

Você vai ter que preencher o "token.txt" com o token do bot, para conseguir ele você vai ter que ir na [aba de aplicações do Discord](https://discord.com/developers/applications/) e [criar um bot novo](https://imgur.com/a/mwQLIJU).

Apesar de opcional, é recomendado preencher o "vips.txt", colocando o ID de todo mundo que vai ter as permissões de usar os comandos especiais do bot.

## Após ligar o bot

Você pode trocar o prefixo padrão com `m!setPrefix -newPrefix (novo prefixo)` e também pode receber um guia de como criar templates usando `prefixo + guide`. Para saber de todos os comandos, use o comando de help, e para criar uma source com imagem é só usar o comando `source` e sem imagem com o comando `add`. 
