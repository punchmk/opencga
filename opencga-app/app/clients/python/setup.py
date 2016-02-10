from setuptools import setup
import uuid
from pip.req import parse_requirements
import os


BASE_DIR = os.path.dirname(__file__)

# some further work is required to get this correctly working
# and distributed. As an interim hack, we insert the requirements
# in below.
# install_reqs = parse_requirements(os.path.join(BASE_DIR, "requirements.txt"), session=uuid.uuid1())
# reqs = [str(ir.req) for ir in install_reqs if ir.req is not None]

print os.path.join(BASE_DIR, 'pyCGA')

setup(
    name='pyCGA',
    packages=['pyCGA', 'pyCGA.Utils'],
    version='0.1.2',
    scripts=[
        os.path.join(BASE_DIR, 'pyCGA/Scripts/pyCGA'),
        os.path.join(BASE_DIR, 'pyCGA/Scripts/pyCGAIdConverter'),
        os.path.join(BASE_DIR, 'pyCGA/Scripts/pyCGAVariantFetcher'),
        os.path.join(BASE_DIR, 'pyCGA/Scripts/pyCGALogin')
    ],
    url='',
    license='',
    author='antonio',
    author_email='antonio.rueda-martin@genomicsengland.co.uk',
    description='',
    install_requires=[
        u"pip>=7.1.2",
        u"requests>=2.7,<=3.0",
        u"avro==1.7.7",
    ],
)
