#!/usr/bin/python
# coding: utf-8 
# /sdcard/sl4a/scripts/webserver.py

import os
import select
import sys
import urllib

import BaseHTTPServer
import CGIHTTPServer


class DroidCGIHandler(CGIHTTPServer.CGIHTTPRequestHandler):

    """Subclassing of CGIHTTPRequestHandler specifically for the Android
    scripting environment to support limited permissions regarding launching
    cgi subprocess. This Handler simply imports the module using the 
    interpretter instance running the webserver.
    """

    def run_cgi(self):
        """Execute a CGI script."""
        path = self.path
        dir, rest = self.cgi_info

        i = path.find('/', len(dir) + 1)
        while i >= 0:
            nextdir = path[:i]
            nextrest = path[i+1:]

            scriptdir = self.translate_path(nextdir)
            if os.path.isdir(scriptdir):
                dir, rest = nextdir, nextrest
                i = path.find('/', len(dir) + 1)
            else:
                break

        # find an explicit query string, if present.
        i = rest.rfind('?')
        if i >= 0:
            rest, query = rest[:i], rest[i+1:]
        else:
            query = ''

        # dissect the part after the directory name into a script name &
        # a possible additional path, to be stored in PATH_INFO.
        i = rest.find('/')
        if i >= 0:
            script, rest = rest[:i], rest[i:]
        else:
            script, rest = rest, ''

        scriptname = dir + '/' + script
        scriptfile = self.translate_path(scriptname)
        if not os.path.exists(scriptfile):
            self.send_error(404, "No such CGI script (%r)" % scriptname)
            return
        if not os.path.isfile(scriptfile):
            self.send_error(403, "CGI script is not a plain file (%r)" %
                            scriptname)
            return
        ispy = self.is_python(scriptname)
        if not ispy:
            if not (self.have_fork or self.have_popen2 or self.have_popen3):
                self.send_error(403, "CGI script is not a Python script (%r)" %
                                scriptname)
                return
            if not self.is_executable(scriptfile):
                self.send_error(403, "CGI script is not executable (%r)" %
                                scriptname)
                return

        # Reference: http://hoohoo.ncsa.uiuc.edu/cgi/env.html
        # XXX Much of the following could be prepared ahead of time!
        env = {}
        env['SERVER_SOFTWARE'] = self.version_string()
        env['SERVER_NAME'] = self.server.server_name
        env['GATEWAY_INTERFACE'] = 'CGI/1.1'
        env['SERVER_PROTOCOL'] = self.protocol_version
        env['SERVER_PORT'] = str(self.server.server_port)
        env['REQUEST_METHOD'] = self.command
        uqrest = urllib.unquote(rest)
        env['PATH_INFO'] = uqrest
        env['PATH_TRANSLATED'] = self.translate_path(uqrest)
        env['SCRIPT_NAME'] = scriptname
        if query:
            env['QUERY_STRING'] = query
        host = self.address_string()
        if host != self.client_address[0]:
            env['REMOTE_HOST'] = host
        env['REMOTE_ADDR'] = self.client_address[0]
        authorization = self.headers.getheader("authorization")
        if authorization:
            authorization = authorization.split()
            if len(authorization) == 2:
                import base64, binascii
                env['AUTH_TYPE'] = authorization[0]
                if authorization[0].lower() == "basic":
                    try:
                        authorization = base64.decodestring(authorization[1])
                    except binascii.Error:
                        pass
                    else:
                        authorization = authorization.split(':')
                        if len(authorization) == 2:
                            env['REMOTE_USER'] = authorization[0]
        # XXX REMOTE_IDENT
        if self.headers.typeheader is None:
            env['CONTENT_TYPE'] = self.headers.type
        else:
            env['CONTENT_TYPE'] = self.headers.typeheader
        length = self.headers.getheader('content-length')
        if length:
            env['CONTENT_LENGTH'] = length
        referer = self.headers.getheader('referer')
        if referer:
            env['HTTP_REFERER'] = referer
        accept = []
        for line in self.headers.getallmatchingheaders('accept'):
            if line[:1] in "\t\n\r ":
                accept.append(line.strip())
            else:
                accept = accept + line[7:].split(',')
        env['HTTP_ACCEPT'] = ','.join(accept)
        ua = self.headers.getheader('user-agent')
        if ua:
            env['HTTP_USER_AGENT'] = ua
        co = filter(None, self.headers.getheaders('cookie'))
        if co:
            env['HTTP_COOKIE'] = ', '.join(co)
        # XXX Other HTTP_* headers
        # Since we're setting the env in the parent, provide empty
        # values to override previously set values
        for k in ('QUERY_STRING', 'REMOTE_HOST', 'CONTENT_LENGTH',
                  'HTTP_USER_AGENT', 'HTTP_COOKIE', 'HTTP_REFERER'):
            env.setdefault(k, "")
        os.environ.update(env)

        self.send_response(200, "Script output follows")

        decoded_query = query.replace('+', ' ')

        if self.have_fork:
            # Unix -- fork as we should
            args = [script]
            if '=' not in decoded_query:
                args.append(decoded_query)
            nobody = CGIHTTPServer.nobody_uid()
            self.wfile.flush() # Always flush before forking
            pid = os.fork()
            if pid != 0:
                # Parent
                pid, sts = os.waitpid(pid, 0)
                # throw away additional data [see bug #427345]
                while select.select([self.rfile], [], [], 0)[0]:
                    if not self.rfile.read(1):
                        break
                if sts:
                    self.log_error("CGI script exit status %#x", sts)
                return
            # Child
            try:
                try:
                    os.setuid(nobody)
                except os.error:
                    pass
                os.dup2(self.rfile.fileno(), 0)
                os.dup2(self.wfile.fileno(), 1)
                #hack to import python scripts due to permission restrictions
                #regarding launching another interpreter instance                                               
                root, ext = os.path.splitext(scriptfile)
                if ext == '.py':
                    root, script = os.path.split(root)
                    sys.path.append(root)
                    __import__(script)
                    os._exit(0)
                else:
                    os.execve(scriptfile, args, os.environ)
            except:
                self.server.handle_error(self.request, self.client_address)
                os._exit(127)

ADDRESS = ''
PORT = 8080

handler = DroidCGIHandler
handler.cgi_directories = ['/cgi-bin']
httpd = BaseHTTPServer.HTTPServer((ADDRESS, PORT), handler)
print "serving at port", PORT
httpd.serve_forever()
