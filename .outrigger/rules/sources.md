# Sources — trust tiers

| Tier | What | Use |
| :-: | :-- | :-- |
| T1 | vendor documentation and standards bodies (MathWorks, ISO, IEC, NIST, OWASP, python.org, Microsoft Learn) | cite directly |
| T2 | recognised community: well-maintained GitHub projects, highly rated MATLAB Central entries, peer-reviewed papers | cite, note the tier |
| T3 | blogs, forums, Q&A, videos | only together with a T1/T2 source that agrees |
| T4 | unknown author, no date, mirrors, paste sites, link shorteners, files from chat or email | **stop and ask** |

The guard keeps an allow-list of hosts in `.outrigger/outrigger.toml` (`[sources] allow_hosts`). Fetching from any
other host is blocked until the user approves.

## Rules

- **S1** Add a source only to answer a question the work actually has — not to look thorough.
- **S2** Open it before you cite it. Record title, author or organisation, date, version or identifier. Never cite from memory.
- **S3** Mark everything you added that the user did not give you with 🆕 and list it in the report.
- **S4** Cite the specific page, not a home page.
- **S5** Paywalled or unreadable → write "not read"; never paraphrase as if read.
- Check every number taken from a source (even "official examples") against the original before using it.

## Supply chain (packages, toolboxes, extensions, MCP servers, skills)

Installing anything is L3. Before asking, present:

```
📦 <name>@<version>   source: <registry / URL>
✅ reputation: <downloads / stars / maintainer / last release>
🔒 known CVEs: <none found in GitHub Advisory DB and NVD / list them>
📝 why: <what it is needed for, what we lose without it>
```
