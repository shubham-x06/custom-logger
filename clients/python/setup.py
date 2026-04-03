from setuptools import setup, find_packages

setup(
    name='custom-logger-client',
    version='1.0.0',
    description='Python Client for the Custom gRPC Logger Event Hub',
    author='Shubham',
    py_modules=['logger_client'],
    install_requires=[
        'grpcio>=1.58.0',
        'grpcio-tools>=1.58.0'
    ],
    python_requires='>=3.7',
    classifiers=[
        'Programming Language :: Python :: 3',
        'License :: OSI Approved :: MIT License',
        'Operating System :: OS Independent',
    ],
)
