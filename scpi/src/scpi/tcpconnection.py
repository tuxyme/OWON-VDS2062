import logging
import asyncio

logger = logging.getLogger(__name__);

class TCPConnection:
    writer: asyncio.StreamWriter;
    reader: asyncio.StreamReader;

    def __init__(self, ip, port):
        asyncio.get_event_loop().run_until_complete(self.connect(ip, port))

    async def connect(self, ipaddr, port):
        self.reader, self.writer = await asyncio.open_connection(ipaddr, port)

    async def write(self, data):
        logger.debug(f"Sending: {data.splitlines()}")
        self.writer.write((data).encode())
        await self.writer.drain()

    async def read(self):
        data = (await self.reader.readline()).rstrip();
        if data[0] == 35:
            size = int.from_bytes(data[1:5], "big")
            logger.debug(f"Receiving {size} bytes")
            data = await self.reader.readexactly(size)
            return data
        logger.debug(f"Received: {data}")
        return data.decode()

    async def disconnect(self):
        if self.writer:
            self.writer.close()
            await self.writer.wait_closed()
