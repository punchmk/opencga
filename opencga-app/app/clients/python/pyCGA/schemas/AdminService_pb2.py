# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: AdminService.proto

import sys
_b=sys.version_info[0]<3 and (lambda x:x) or (lambda x:x.encode('latin1'))
from google.protobuf import descriptor as _descriptor
from google.protobuf import symbol_database as _symbol_database
from google.protobuf import descriptor_pb2
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


import GenericService_pb2 as GenericService__pb2


DESCRIPTOR = _descriptor.FileDescriptor(
  name='AdminService.proto',
  package='org.opencb.opencga.storage.server.grpc',
  syntax='proto3',
  serialized_pb=_b('\n\x12\x41\x64minService.proto\x12&org.opencb.opencga.storage.server.grpc\x1a\x14GenericService.proto2\xf0\x01\n\x0c\x41\x64minService\x12p\n\x06status\x12/.org.opencb.opencga.storage.server.grpc.Request\x1a\x33.org.opencb.opencga.storage.server.grpc.MapResponse\"\x00\x12n\n\x04stop\x12/.org.opencb.opencga.storage.server.grpc.Request\x1a\x33.org.opencb.opencga.storage.server.grpc.MapResponse\"\x00\x42\x13\x42\x11\x41\x64minServiceModelb\x06proto3')
  ,
  dependencies=[GenericService__pb2.DESCRIPTOR,])
_sym_db.RegisterFileDescriptor(DESCRIPTOR)





DESCRIPTOR.has_options = True
DESCRIPTOR._options = _descriptor._ParseOptions(descriptor_pb2.FileOptions(), _b('B\021AdminServiceModel'))
import abc
import six
from grpc.beta import implementations as beta_implementations
from grpc.framework.common import cardinality
from grpc.framework.interfaces.face import utilities as face_utilities

class BetaAdminServiceServicer(six.with_metaclass(abc.ABCMeta, object)):
  """<fill me in later!>"""
  @abc.abstractmethod
  def status(self, request, context):
    raise NotImplementedError()
  @abc.abstractmethod
  def stop(self, request, context):
    raise NotImplementedError()

class BetaAdminServiceStub(six.with_metaclass(abc.ABCMeta, object)):
  """The interface to which stubs will conform."""
  @abc.abstractmethod
  def status(self, request, timeout):
    raise NotImplementedError()
  status.future = None
  @abc.abstractmethod
  def stop(self, request, timeout):
    raise NotImplementedError()
  stop.future = None

def beta_create_AdminService_server(servicer, pool=None, pool_size=None, default_timeout=None, maximum_timeout=None):
  import GenericService_pb2
  request_deserializers = {
    ('org.opencb.opencga.storage.server.grpc.AdminService', 'status'): GenericService_pb2.Request.FromString,
    ('org.opencb.opencga.storage.server.grpc.AdminService', 'stop'): GenericService_pb2.Request.FromString,
  }
  response_serializers = {
    ('org.opencb.opencga.storage.server.grpc.AdminService', 'status'): GenericService_pb2.MapResponse.SerializeToString,
    ('org.opencb.opencga.storage.server.grpc.AdminService', 'stop'): GenericService_pb2.MapResponse.SerializeToString,
  }
  method_implementations = {
    ('org.opencb.opencga.storage.server.grpc.AdminService', 'status'): face_utilities.unary_unary_inline(servicer.status),
    ('org.opencb.opencga.storage.server.grpc.AdminService', 'stop'): face_utilities.unary_unary_inline(servicer.stop),
  }
  server_options = beta_implementations.server_options(request_deserializers=request_deserializers, response_serializers=response_serializers, thread_pool=pool, thread_pool_size=pool_size, default_timeout=default_timeout, maximum_timeout=maximum_timeout)
  return beta_implementations.server(method_implementations, options=server_options)

def beta_create_AdminService_stub(channel, host=None, metadata_transformer=None, pool=None, pool_size=None):
  import GenericService_pb2
  request_serializers = {
    ('org.opencb.opencga.storage.server.grpc.AdminService', 'status'): GenericService_pb2.Request.SerializeToString,
    ('org.opencb.opencga.storage.server.grpc.AdminService', 'stop'): GenericService_pb2.Request.SerializeToString,
  }
  response_deserializers = {
    ('org.opencb.opencga.storage.server.grpc.AdminService', 'status'): GenericService_pb2.MapResponse.FromString,
    ('org.opencb.opencga.storage.server.grpc.AdminService', 'stop'): GenericService_pb2.MapResponse.FromString,
  }
  cardinalities = {
    'status': cardinality.Cardinality.UNARY_UNARY,
    'stop': cardinality.Cardinality.UNARY_UNARY,
  }
  stub_options = beta_implementations.stub_options(host=host, metadata_transformer=metadata_transformer, request_serializers=request_serializers, response_deserializers=response_deserializers, thread_pool=pool, thread_pool_size=pool_size)
  return beta_implementations.dynamic_stub(channel, 'org.opencb.opencga.storage.server.grpc.AdminService', cardinalities, options=stub_options)
# @@protoc_insertion_point(module_scope)
