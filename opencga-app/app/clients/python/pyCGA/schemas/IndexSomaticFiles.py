import os
import sys
from pyCGA.CatalogWS import Files, Samples

path_scope = sys.argv[1]
name_scope = sys.argv[2]
studyId = sys.argv[3]
out_path = sys.argv[4]
index_file_path = sys.argv[5]

file_instance = Files()
sample_instance = Samples()

r = file_instance.search(studyId=studyId,
                     name="~" + name_scope,
                     path="~" + path_scope,
                     include="projects.studies.files.uri,projects.studies.files.index,projects.studies.files.name,projects.studies.files.path,projects.studies.files.sampleIds"
                     )
prev = ""
for f in r:
    print f["name"]
    if "index" not in f:
        sample_id = str(f["sampleIds"][0])
        sample_obj = sample_instance.info(sampleId=sample_id)[0]
        sample_name = sample_obj["name"]
        print sample_name
        dir_path = os.path.join(out_path, sample_name)
        file_id = str(f["id"])

        folder_objs = file_instance.search(studyId=studyId, path=dir_path)
        if folder_objs:
            folder_obj = folder_objs[0]
        else:
            folder_obj = file_instance.create_folder(studyId=studyId, folder=dir_path)[0]
        print folder_obj["path"]
        folder_id = str(folder_obj["id"])

        job_name = "index_" + sample_name

        cmd = "qsub"

        if prev != "":
            cmd += " -hold_jid " + prev

        cmd += " -N " + job_name + " " + index_file_path + " " +\
               " ".join([file_id, folder_id, file_instance.session_id])

        os.system("echo " + cmd)
        # os.system(cmd)

        prev = job_name


# fd = open(list_of_files)
# prev = None
# for line in fd:
#     file_name = line.rstrip("/n")
#     sample_name = file_name.split("/")[6]
#      = file_name.split("/")[6]
