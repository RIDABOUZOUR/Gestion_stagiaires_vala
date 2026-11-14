# =====================================================================
# Frontend Angular (SSR Node) — build multi-stage
# =====================================================================

# ---- Stage 1 : build Angular ----
FROM node:20-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# URL publique du backend injectée au build (voir docker-compose.yml).
# On régénère entièrement le fichier d'environnement : plus robuste qu'un sed
# qui dépendrait du contenu existant.
ARG API_URL=http://localhost:8080
RUN printf '%s\n' \
      'export const environment = {' \
      '  production: true,' \
      "  apiUrl: '${API_URL}'" \
      '};' > src/environments/environment.prod.ts

RUN npm run build

# ---- Stage 2 : runtime Node ----
FROM node:20-alpine AS runtime
WORKDIR /app
ENV NODE_ENV=production

# Utilisateur non-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Dépendances de production uniquement (express requis par le SSR)
COPY package.json package-lock.json ./
RUN npm ci --omit=dev && npm cache clean --force

COPY --from=build /app/dist ./dist
RUN chown -R appuser:appgroup /app

USER appuser
EXPOSE 4000
ENV PORT=4000

CMD ["node", "dist/frontend_angular/server/server.mjs"]
