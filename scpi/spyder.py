#!/usr/bin/env python3

import matplotlib.pyplot as plt
import numpy as np
from scpi.scpilink import SCPILink
import nest_asyncio
nest_asyncio.apply()

dev = SCPILink("127.0.0.1", 25001)

ch1data = dev.get_adc("CH1", 15)
ch1data = [x for x in ch1data]
ch1data = np.array(ch1data, dtype=np.int8)

plt.plot(ch1data)
plt.show();

