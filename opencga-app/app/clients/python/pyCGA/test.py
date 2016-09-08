import os
from unittest import TestCase

from pyCGA.CatalogWS import Users, Files, Samples, Individuals
from pyCGA.Exceptions import ServerResponseException

file_to_register = '/genomes/analysis/rare_disease/50000578/1467626646/LP2000268-DNA_A06/Variations/LP2000268-DNA_A06.tiering.json'
folder_to_register = '/genomes/analysis/rare_disease/50000578/1467626646/LP2000268-DNA_A06/logs'
sample_to_register = 'LP2000268-DNA_A06'
test_folder = 'test/'
model_to_create_annotation_set = ''
data_to_register = ''
host = 'http://10.0.32.42:8080/'
catalog_instance = 'opencga_08'
pwd = 'gel'
study_id = 'ts'
individual_to_register = '500000521'

## Testing login
unregistered_session = {"host": host, "sid": ""}
user = Users(unregistered_session, instance=catalog_instance)

## Testing file registration

class WorkflowTestCase(TestCase):

    def setUp(self):
        unregistered_session = {"host": host, "sid": ""}
        self.user = Users(unregistered_session, instance=catalog_instance)
        sid = self.user.login_method("gel", pwd)[0]["sessionId"]
        self.token = {"user": "gel", "host": host, "sid": str(sid), "instance": catalog_instance, "debug": True}
        self.file_connector = Files(token=self.token)
        self.sample_connector = Samples(token=self.token)
        self.individual_connector = Individuals(token=self.token)

    # def tearDown(self):
    #     self.sample_connector.delete(sample_to_register, force='true')



    def test_register_file(self):

        # create_folder = self.file_connector.create_folder(studyId=study_id, folder=test_folder)
        # registered_files = self.file_connector.link(studyId=study_id, uri=file_to_register,
        #                                             path=test_folder, parents=True, createFolder=False)
        # print registered_files
        # self.assertEqual(len(registered_files), 1)
        # self.assertEqual(registered_files[0]['uri'], 'file://' + file_to_register)

        # self.file_connector.unlink(str(7))
        for line in self.file_connector.general_method(ws_category1='files',
                                                       action='download',
                                                       item_id1=os.path.basename(file_to_register)):
            print line





    def test_create_sample(self):
        created_sample = self.sample_connector.create(studyId=study_id, name=sample_to_register, description=sample_to_register, source='Manual')
        self.assertEqual(created_sample[0]['name'], sample_to_register)

    # def test_create_individual(self):
    #     created_individual = self.sample_connector.create(studyId=study_id, name=individual_to_register)



















