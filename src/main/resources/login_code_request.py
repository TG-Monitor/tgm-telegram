#!/usr/bin/env python3
#
# Start login procedure by requesting a login code to be sent to the user's
# device (by SMS or Telegram).
#
# The following scenarios are valid use cases:
#   1) The session is not logged in (normal case)
#   2) The session is already logged in
#
# The script handles them in the following way:
#   1) Start login procedure as usual
#   2) Print "null" to stdout
#
# The script writes the following to stdout on completion:
#  - The "phone code hash" for this login procedure, in case 1)
#  - "null", in case 2)
#------------------------------------------------------------------------------#

import telethon.sync
from telethon import TelegramClient
import sys

api_id = sys.argv[1]
api_hash = sys.argv[2]
session_name = sys.argv[3]
phone = sys.argv[4]

client = TelegramClient(session_name, api_id, api_hash)
client.connect()

# Case 2): session is already logged in
if client.is_user_authorized():
    print("null")
    exit()

# Case 1): session is not logged in
res = client.send_code_request(phone)
# Example 'res' object (display with 'print(res.stringify())'):
# SentCode(
#         type=SentCodeTypeSms(
#                 length=5
#         ),
#         phone_code_hash='c6ff14a654682544ed',
#         phone_registered=True,
#         next_type=CodeTypeCall(
#         ),
#         timeout=120,
#         terms_of_service=None
# )
print(res.phone_code_hash)
