docker compose -f docker-compose.yml build &&
echo "imagem buildada para execução" &&
docker stack rm poupacompra &&

sleep 5

echo "removido qualquer stack de traefik no ambiente docker" &&
docker stack deploy -c docker-compose.yml poupacompra &&
echo "realizado deploy da stack docker swarm"