#!/usr/bin/env python3
#
# This script makes sure the specified session file is deleted, thus logging
# the user out from this session.
#
# The following scenarios are valid use cases:
#   1) Session exists and is logged in
#   2) Session exists but is not logged in
#   3) Session doesn't exist
#
# And the script handles them in the following way: 
#   1) Session is deleted
#   2) Session is deleted
#   3) Nothing is done
#------------------------------------------------------------------------------#

import telethon.sync
from telethon import TelegramClient
import sys
import os

api_id = sys.argv[1]
api_hash = sys.argv[2]
session_name = sys.argv[3]

# Case 3) above
if not os.path.exists(session_name + ".session"):
    exit()

# Case 1) and 2) above
client = TelegramClient(session_name, api_id, api_hash)
client.connect()
client.log_out()
