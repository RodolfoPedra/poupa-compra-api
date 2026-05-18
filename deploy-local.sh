docker compose build &&
echo "imagem buildada para execução" &&
docker compose down &&

sleep 5

echo "removido qualquer stack de traefik no ambiente docker" &&
docker compose up -d &&
echo "realizado deploy da stack docker compose"