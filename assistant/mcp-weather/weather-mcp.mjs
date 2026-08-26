#!/usr/bin/env node
/**
 * Portable MCP weather server — Node.js 18+, no npm install, no compile.
 *
 * CLI (test):  node weather-mcp.mjs Warszawa
 * MCP server:  node weather-mcp.mjs --mcp
 *              (also auto-starts when stdin is a pipe, e.g. Claude Desktop)
 *
 * Put a .env file next to this script:
 *   WEATHER_API_URL=https://api.weatherapi.com/v1/current.json
 *   WEATHER_API_KEY=your_key
 */

import { createInterface } from "node:readline";
import { readFileSync, existsSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const SERVER_NAME = "semdin-weather-mcp";
const SERVER_VERSION = "1.0.0";
const DEFAULT_API_URL = "https://api.weatherapi.com/v1/current.json";
const SCRIPT_DIR = dirname(fileURLToPath(import.meta.url));

loadEnvFile(join(SCRIPT_DIR, ".env"));

const WEATHER_API_URL = process.env.WEATHER_API_URL || DEFAULT_API_URL;
const WEATHER_API_KEY = process.env.WEATHER_API_KEY;

const args = process.argv.slice(2).filter((a) => a !== "--mcp");
const forceMcp = process.argv.includes("--mcp");

if (!forceMcp && (process.stdin.isTTY || args.length > 0)) {
  await runCli(args);
} else {
  runMcpServer();
}

async function runCli(cliArgs) {
  if (cliArgs.length === 0 || cliArgs.includes("--help") || cliArgs.includes("-h")) {
    printUsage();
    process.exit(cliArgs.length === 0 ? 1 : 0);
  }

  const city = cliArgs.join(" ");
  const result = await getWeather(city);

  if (result.ok) {
    process.stdout.write(result.text + "\n");
    process.exit(0);
  }

  process.stderr.write(result.text + "\n");
  process.exit(1);
}

function printUsage() {
  process.stdout.write(`MCP Weather — portable Node.js script (no compile, no npm)

Usage:
  node weather-mcp.mjs <city>     fetch current temperature
  node weather-mcp.mjs --mcp      start MCP server on stdio

Claude Desktop config:
  {
    "mcpServers": {
      "weather": {
        "command": "node",
        "args": ["${join(SCRIPT_DIR, "weather-mcp.mjs").replace(/\\/g, "/")}"]
      }
    }
  }

Requires Node.js 18+ and WEATHER_API_KEY in .env next to this file
or in the environment. Get a free key: https://www.weatherapi.com/signup.aspx
`);
}

function runMcpServer() {
  const rl = createInterface({ input: process.stdin, crlfDelay: Infinity });

  rl.on("line", async (line) => {
    const trimmed = line.trim();
    if (!trimmed) return;

    let message;
    try {
      message = JSON.parse(trimmed);
    } catch {
      send({
        jsonrpc: "2.0",
        id: null,
        error: { code: -32700, message: "Parse error" },
      });
      return;
    }

    try {
      await handleMessage(message);
    } catch (err) {
      if (message.id !== undefined && message.id !== null) {
        send({
          jsonrpc: "2.0",
          id: message.id,
          error: { code: -32603, message: err?.message || "Internal error" },
        });
      }
    }
  });

  rl.on("close", () => process.exit(0));
}

async function handleMessage(message) {
  const { id, method, params } = message;

  if (!method) return;

  const isNotification = id === undefined || id === null;
  if (isNotification) return;

  switch (method) {
    case "initialize":
      send({
        jsonrpc: "2.0",
        id,
        result: {
          protocolVersion: params?.protocolVersion || "2024-11-05",
          capabilities: { tools: {} },
          serverInfo: { name: SERVER_NAME, version: SERVER_VERSION },
        },
      });
      return;

    case "ping":
      send({ jsonrpc: "2.0", id, result: {} });
      return;

    case "tools/list":
      send({
        jsonrpc: "2.0",
        id,
        result: {
          tools: [
            {
              name: "get-weather",
              description: "get weather info from weatherapi",
              inputSchema: {
                type: "object",
                properties: {
                  city: {
                    type: "string",
                    description: "name of the city (e.g. mardin)",
                  },
                },
                required: ["city"],
              },
            },
          ],
        },
      });
      return;

    case "tools/call": {
      const name = params?.name;
      const city = params?.arguments?.city;

      if (name !== "get-weather") {
        send({
          jsonrpc: "2.0",
          id,
          result: {
            content: [{ type: "text", text: "unknown tool" }],
            isError: true,
          },
        });
        return;
      }

      if (typeof city !== "string" || !city.trim()) {
        send({
          jsonrpc: "2.0",
          id,
          result: {
            content: [{ type: "text", text: "city is required" }],
            isError: true,
          },
        });
        return;
      }

      const result = await getWeather(city.trim());
      send({
        jsonrpc: "2.0",
        id,
        result: {
          content: [{ type: "text", text: result.text }],
          isError: !result.ok,
        },
      });
      return;
    }

    default:
      send({
        jsonrpc: "2.0",
        id,
        error: { code: -32601, message: `Method not found: ${method}` },
      });
  }
}

async function getWeather(city) {
  if (!WEATHER_API_KEY || WEATHER_API_KEY === "your_api_key_here") {
    return {
      ok: false,
      text: "Missing WEATHER_API_KEY. Put it in .env next to weather-mcp.mjs",
    };
  }

  const reqUrl = `${WEATHER_API_URL}?key=${WEATHER_API_KEY}&q=${encodeURIComponent(city)}&aqi=no`;

  try {
    const data = await fetch(reqUrl);
    if (!data.ok) {
      return { ok: false, text: "Some error occured." };
    }

    const jsonData = await data.json();
    const temp = jsonData?.current?.temp_c;
    if (temp === undefined) {
      return { ok: false, text: "Some error occured." };
    }

    return {
      ok: true,
      text: `the weather in ${city} is currently: ${temp}`,
    };
  } catch (err) {
    return { ok: false, text: err?.message || "Some error occured." };
  }
}

function send(payload) {
  process.stdout.write(JSON.stringify(payload) + "\n");
}

function loadEnvFile(filePath) {
  if (!existsSync(filePath)) return;

  const content = readFileSync(filePath, "utf8");
  for (const rawLine of content.split(/\r?\n/)) {
    const line = rawLine.trim();
    if (!line || line.startsWith("#")) continue;

    const eq = line.indexOf("=");
    if (eq <= 0) continue;

    const key = line.slice(0, eq).trim();
    let value = line.slice(eq + 1).trim();
    if (
      (value.startsWith('"') && value.endsWith('"')) ||
      (value.startsWith("'") && value.endsWith("'"))
    ) {
      value = value.slice(1, -1);
    }

    if (process.env[key] === undefined) {
      process.env[key] = value;
    }
  }
}
