import socket

server_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_sock.bind(('localhost', 3111))
server_sock.listen(1)

client, address = server_sock.accept()
print(client.recv(1024).decode())
client.close()
server_sock.close()
