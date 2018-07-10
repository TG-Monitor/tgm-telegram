#!/usr/bin/env python3
#
# Test whether specified session is logged in.
#
# The following scenarios are valid use cases:
#   1) The session file exists and the session is logged in
#   2) The session file exists and the session is logged out
#   3) The session file doesn't exist
#
# The script handles these cases in the following way:
#   1) Session is logged in: print "true"
#   2) Session is logged out: print "false"
#   3) Session is logged out: print "false"
#
# Output: the script prints to stdout "true" if the session is logged in, and
# "false" otherwise.
#------------------------------------------------------------------------------#

import telethon.sync
from telethon import TelegramClient
import sys
import os.path

api_id = sys.argv[1]
api_hash = sys.argv[2]
session_name = sys.argv[3]

if not os.path.exists(session_name + ".session"):
    print("false")
else:
    client = TelegramClient(session_name, api_id, api_hash)
    client.connect()
    if client.is_user_authorized():
        print("true")
    else:
        print("false")
