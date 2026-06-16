# Reinicia o serviço backup
```bash
docker compose up -d --force-recreate backup
```

# Aguarda ~5 segundos e verifica se está running (não restarting)
```bash
docker compose ps backup
```

# Executa o backup manualmente para testar
```bash
docker compose exec backup sh /backup.sh
```

# Verifica se os ficheiros .dump apareceram
```bash
ls ./backups/
```

# HOW TO RESTORE

Para restaurar um dos backups usarias pg_restore. Tens de correr dentro do mesmo container backup para teres o comando disponível:

BD remota (ArcadeHaven):


docker compose exec backup pg_restore `
  -h vsgate-s1.dei.isep.ipp.pt `
  -p 10345 `
  -U $env:SPRING_DATASOURCE_USERNAME `
  -d postgres `
  --no-password `
  /backups/arcadehaven_2026-06-16_01-07-40.dump
BD local Keycloak:


docker compose exec backup pg_restore `
  -h keycloak-db `
  -p 5432 `
  -U keycloak `
  -d keycloak `
  --no-password `
  /backups/keycloak_2026-06-16_01-07-40.dump
A password é pedida interativamente — ou podes passar com PGPASSWORD=... antes do comando, igual ao que o backup.sh faz.

Nota: pg_restore com formato custom (--format=custom) não executa SQL diretamente, restaura tabela a tabela de forma eficiente. Se a BD de destino já tiver dados e quiseres fazer um restore limpo, adicionas --clean para dropar os objetos antes de recriar.