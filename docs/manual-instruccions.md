# Manual d'instruccions

Aquest manual descriu com executar RuleRefactor en mode CLI, en mode Agent API, i com fer la validació premerge.

## 1) Requisits

- Java 21
- Maven Wrapper disponible (`./mvnw`)

Comprovació ràpida:

```bash
java -version
./mvnw -v
```

## 2) Build del projecte

```bash
./mvnw clean test package
```

Artefacte principal:

- `target/rulerefactor-0.1.0-SNAPSHOT.jar`

## 3) CLI bàsic

Ajuda:

```bash
java -jar target/rulerefactor-0.1.0-SNAPSHOT.jar --help
```

### 3.1 Llistar issues

```bash
java -jar target/rulerefactor-0.1.0-SNAPSHOT.jar list-issues --path . --format table
java -jar target/rulerefactor-0.1.0-SNAPSHOT.jar list-issues --path . --rules java:S1155,java:S2293 --format json
```

### 3.2 Planificar fixes

```bash
java -jar target/rulerefactor-0.1.0-SNAPSHOT.jar plan --path . --rules java:S2293
```

### 3.3 Aplicar fixes

Dry-run:

```bash
java -jar target/rulerefactor-0.1.0-SNAPSHOT.jar apply --path . --rules java:S2293 --dry-run
```

Apply real:

```bash
java -jar target/rulerefactor-0.1.0-SNAPSHOT.jar apply --path . --rules java:S2293
```

Apply amb verificació (`compile`, `test`):

```bash
java -jar target/rulerefactor-0.1.0-SNAPSHOT.jar apply --path . --rules java:S2293 --verify compile,test
```

Notes:

- Si falla una verificació, els canvis de la transformació es fan rollback.
- Els issues obsolets es marquen com `skipped_outdated`.

## 4) Mode Agent API (JSON no interactiu)

Executar amb fitxer JSON:

```bash
java -jar target/rulerefactor-0.1.0-SNAPSHOT.jar agent-api --input docs/request-premerge.json
```

Referències:

- Schema: `docs/agent-api-schema.json`
- Exemples: `docs/agent-api-examples.md`

## 5) Validació premerge

Script oficial:

```bash
./scripts/premerge-check.sh
```

Què fa:

1. `clean package`
2. Executa `agent-api` amb `docs/request-premerge.json`
3. Desa output a `target/premerge-agent-response.json`
4. Falla amb `exit 1` si `failed > 0`

## 6) Onboarding de noves regles

Segueix la plantilla:

- `docs/rule-onboarding-template.md`

Inclou:

- metadata (`ruleKey`, `detectable`, `fixable`, `confidence`)
- detector/fixer
- registre de capability
- tests de detecció, apply i idempotència

## 7) Resolució d'incidències

- **Error Java version**: assegura Java 21.
- **Testos lents**: és normal en suites de transformació; torna a executar `./mvnw test`.
- **No canvis aplicats**: comprova si la regla és detectada i fixable al catàleg (`docs/detectable-java-rules.md`).
