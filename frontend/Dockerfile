# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM node:24-alpine AS build

WORKDIR /workspace

COPY package.json package-lock.json ./

# `npm ci` falha aqui: o lockfile foi gerado no Windows, e o fallback WASM do
# lightningcss para musl/Alpine (pacotes @emnapi/*, dependência de peer de
# @napi-rs/wasm-runtime) nunca foi resolvido nele, então `npm ci` reclama que
# está "faltando" no lock. `npm install` resolve normalmente porque não exige
# que o lockfile já contenha exatamente essas entradas específicas da
# plataforma — só afeta o filesystem efêmero desta stage, o lockfile do repo
# não é alterado.
RUN npm install

COPY . .

RUN npm run build

# ---- Runtime stage ----
# nginx-unprivileged já roda como usuário não-root e escuta em 8080 por padrão,
# sem precisar de ajustes extras de permissão (diferente da imagem nginx normal).
FROM nginxinc/nginx-unprivileged:alpine AS runtime

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/dist/climaservice-web/browser /usr/share/nginx/html

EXPOSE 8080
