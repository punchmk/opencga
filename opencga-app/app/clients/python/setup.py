from distutils.core import setup
import uuid
from pip.req import parse_requirements
import os
install_reqs = parse_requirements(os.path.join(os.path.dirname(__file__), "requirements.txt"), session=uuid.uuid1())
reqs = [str(ir.req) for ir in install_reqs if ir.req is not None]
setup(
    name='pyCGA-0.8',
    packages=['pyCGA', 'pyCGA.Utils'],
    version='0.1.0',
    scripts=[os.path.join(os.path.dirname(__file__),'pyCGA/Scripts/pyCGA'), os.path.join(os.path.dirname(__file__),'pyCGA/Scripts/pyCGAIdConverter'),
             os.path.join(os.path.dirname(__file__),'pyCGA/Scripts/pyCGAVariantFetcher'), os.path.join(os.path.dirname(__file__),'pyCGA/Scripts/pyCGALogin')],
    url='',
    license='',
    author='antonio',
    author_email='antonio.rueda-martin@genomicsengland.co.uk',
    description='',
    install_requires=reqs,
)
