# -*- coding: utf-8 -*-
from setuptools import setup, find_packages

try:
    long_description = open("README.rst").read()
except IOError:
    long_description = ""
#with open('../../src/main/resources/_KVDN_VERSION.txt', 'r') as myfile:
#    version = myfile.read().replace('\n', '')
version = 2.14
setup(
    name="kvdnc",
    version=version,
    description="kvdn client library and tool",
    license="Apache",
    author="Grant Haywood",
    packages=["kvdn_client", "kvdn_pillar"],
    install_requires=["requests","six","argparse","persist-queue", "sqlitedict"],

    long_description=long_description,
    entry_points={
        'console_scripts': [
            'kvdn-cli = kvdn_client.__main__:main'
        ]
    },
    classifiers=[
        "Programming Language :: Python",
        "Programming Language :: Python :: 2.7",
        "Programming Language :: Python :: 3.6",
    ]
)
