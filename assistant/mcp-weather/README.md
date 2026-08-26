# Portable weather MCP

Portable version of stdio MCP server for current temperature.
Build from [semdin/mcp-weather](https://github.com/semdin/mcp-weather). 
Can be run with plain Node.js 18+ (no need for `npm install`, TypeScript, or `npx`).

The assistant launches `weather-mcp.mjs --mcp` and injects `WEATHER_API_KEY`.
You do not need a `.env` file for the chat UI.

Manual check:

```powershell
$env:WEATHER_API_KEY = "..."
node weather-mcp.mjs Warsaw
```
