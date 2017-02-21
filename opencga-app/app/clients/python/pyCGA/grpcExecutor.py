import json
import os
from grpc.beta import implementations
from pyCGA.schemas.GenericService_pb2 import Request
from pyCGA.Exceptions import LoginException
from pyCGA.schemas.VariantService_pb2 import beta_create_VariantService_stub
from pathlib import Path

SERVICES = {
    "variants": beta_create_VariantService_stub
}


class ProtoServices(object):
    def __init__(self, token=None, version="v1", instance="opencga"):

        home = Path(os.getenv("HOME"))

        if token is None:
            opencga_dir = home.joinpath(".opencga", "openCGA.json")
            if not Path.exists(opencga_dir):
                raise LoginException()
            fd = open(opencga_dir.as_posix())
            session = json.load(fd)
        else:
            session = token

        self.session_id = session["sid"]
        self.host = session["host"]
        self.debug_path = home.joinpath(".opencga", "pyCGA.log").as_posix()

        if "debug" in session:
            self.debug = session["debug"]
        else:
            self.debug = False

        if "instance" in session:
            self.instance = session["instance"]
        else:
            self.instance = instance

    def execute_query(self, service, database, options, query_options):
        host_part = self.host.split("\t")
        port = host_part[-1]
        host = ":".join(host_part[:-1])

        channel = implementations.insecure_channel(host, port)
        request = Request(storageEngine="mongodb", database=database, query=options, query_options=query_options)
        stub = SERVICES[service](channel)
        _TIMEOUT_SECONDS = 10
        response = stub.get(request, _TIMEOUT_SECONDS)
        for result in response:
            yield result