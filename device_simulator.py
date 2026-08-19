#!/usr/bin/env python3

import socket
import struct
import threading
import time
import logging
import sys

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(threadName)s] %(message)s")

HOST = "0.0.0.0"
PORT = 1500

PRINT_MESSAGES = False

def handle_client(conn, addr):
    conn.setsockopt(socket.IPPROTO_TCP, socket.TCP_NODELAY, 1)
    logging.info(f"New Connection: {addr}")
    
    try:
        while True:
            # We use a short timeout so we can detect if the socket was killed
            conn.settimeout(0.5)
            try:
                data = conn.recv(4096)
            except (socket.timeout, OSError):
                continue
            
            if not data: break
            
            requestMsgBytes = data[2:]  # first two are the packet length bytes (we are not using those here just for simplicity)
            if PRINT_MESSAGES:
                requestMsgStr = requestMsgBytes.decode("utf8", errors="replace")
                logging.info(f"received message: {requestMsgStr}")
            
            responseMsgBytes = b'DEVICE RESPONSE - ECHO: ' + requestMsgBytes
            if PRINT_MESSAGES:
                responseMsgStr = responseMsgBytes.decode("utf8", errors="replace")
                logging.info(f"response message: {responseMsgStr}")

            full_response = struct.pack(">H", len(responseMsgBytes)) + responseMsgBytes

            # IMMEDIATELY AFTER this send.
            time.sleep(0.002)
            conn.sendall(full_response)
            
    except Exception as e:
        logging.debug(f"Connection closed for {addr}: {e}")
    finally:
        try: conn.close()
        except: pass

def main():
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind((HOST, PORT))
    server.listen(100)
    logging.info(f"Simulator ready on port {PORT}. Persistence TTL")

    try:
        while True:
            server.settimeout(1.0)
            try:
                client_sock, client_addr = server.accept()
                threading.Thread(target=handle_client, args=(client_sock, client_addr), daemon=True).start()
            except socket.timeout:
                continue
    except KeyboardInterrupt:
        sys.exit(0)

if __name__ == "__main__":
    main()
