#!/usr/bin/env python3
#
# Print all incoming messages of a specific peer (group or channel) to stdout.
# Each message is printed as a single line of JSON.
#
# Prerequisite for running this script: the supplied session is logged in.
#------------------------------------------------------------------------------#

from telethon import TelegramClient, events
import sys
import json

import logging
logging.basicConfig(level=logging.ERROR) 

api_id = sys.argv[1]
api_hash = sys.argv[2]
session_name = sys.argv[3]
peer = sys.argv[4]

client = TelegramClient(session_name, api_id, api_hash)

@client.on(events.NewMessage(incoming=True, chats=(peer)))
async def handler(event):
    message = event.message
    data = {}
    # Keys must coincide with field names in TelegramMessage class
    data['id'] = message.id
    data['date'] = int(message.date.timestamp())
    data['text'] = message.message
    data['replyMessageId'] = await get_reply_message_id(message)
    data['sender'] = await get_sender(message)
    data['peer'] = await get_peer(message)
    #data['media'] = await get_media(message)
    data['audio'] = has_media(message, 'audio')
    data['document'] = has_media(message, 'document')
    data['gif'] = has_media(message, 'gif')
    data['photo'] = has_media(message, 'photo')
    data['sticker'] = has_media(message, 'sticker')
    data['video'] = has_media(message, 'video')
    data['voice'] = has_media(message, 'voice')
    # Print to stdout (flush flag needed to make output readable by Java)
    print(json.dumps(data), flush=True)
    #print(data, flush=True)

async def get_sender(message):
    sender = await message.get_sender()
    sender_dict = {}
    # Keys must coincide with field names in the TelegramMessage.Sender class
    sender_dict['id'] = sender.id
    sender_dict['isBot'] = sender.bot
    sender_dict['isContact'] = sender.contact
    sender_dict['firstName'] = sender.firstName
    sender_dict['lastName'] = sender.last_name
    sender_dict['username'] = sender.username
    sender_dict['phone'] = sender.phone
    return sender_dict

async def get_peer(message):
    peer = await message.get_chat()
    peer_dict = {}
    # Keys must coincide with field names in the TelegramMessage.Peer class
    peer_dict['id'] = peer.id
    peer_dict['title'] = peer.title
    peer_dict['username'] = peer.username
    peer_dict['participantsCount'] = peer.participants_count
    peer_dict['megagroup'] = peer.megagroup
    return peer_dict

async def get_reply_message_id(message):
    reply_message = await message.get_reply_message()
    if reply_message is None:
        reply_message_id = None
    else:
        reply_message_id = reply_message.id
    return reply_message_id

# Don't handle media yet. This is just a placeholder. If it will be really
# necessary, it should be possible to download media into some directory, and
# add its URI as a field to the response (e.g. {..."media": "/foo/bar.jpg"...})
def has_media(message, kind):
    return getattr(message, kind) is not None
#def get_media(message, kind):
#    media = getattr(message, kind)
#    if media is not None:
#        # Check Document and Photo types
#        doc_dict = {}
#        doc_dict['id'] = media.id
#        doc_dict['access_hash'] = media.access_hash
#        if kind is not 'photo':
#            doc_dict['mime_type'] = media.mime_type
#            doc_dict['size'] = media.size
#        return doc_dict
#    else:
#        return None

with client.start():
    client.run_until_disconnected()
