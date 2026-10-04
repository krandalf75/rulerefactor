# Agent API Examples

## Select by rule keys

```json
{
  "mode": "apply",
  "projectPath": ".",
  "dryRun": false,
  "verify": ["compile"],
  "selection": {
    "ruleKeys": ["java:S2293", "java:S1155"]
  }
}
```

## Select by issue IDs

```json
{
  "mode": "apply",
  "projectPath": ".",
  "dryRun": true,
  "selection": {
    "issueIds": ["ISSUE-1", "ISSUE-2"]
  }
}
```

## Execute from CLI

```bash
java -jar target/rulerefactor-0.1.0-SNAPSHOT.jar agent-api --input docs/request.json
```
