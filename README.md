# Nva Bare Proxy

The purpose of this project is to fetch authority-metadata by given parameters like name or/and feideId (GET request).
As well it is expected that authority-metadata can be updated by a POST request updating/adding feideId, orcId or orgunitid to an existing authority identified by its scn (aka. System Control Number).
Last but not least one can POST a 'name' to create a new authority in BARE. 
 
The application uses several AWS resources, including Lambda functions and an API Gateway API. These resources are defined in the `template.yaml` file in this project. You can update the template to add AWS resources through the same deployment process that updates your application code.

## Deploy the sample application

The Serverless Application Model Command Line Interface (SAM CLI) is an extension of the AWS CLI that adds functionality for building and testing Lambda applications. It uses Docker to run your functions in an Amazon Linux environment that matches Lambda. It can also emulate your application's build environment and API.

To use the SAM CLI, you need the following tools.

* AWS CLI - [Install the AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html) and [configure it with your AWS credentials].
* SAM CLI - [Install the SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html)
* Java8 - [Install the Java SE Development Kit 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

The SAM CLI uses an Amazon S3 bucket to store your application's deployment artifacts. If you don't have a bucket suitable for this purpose, create one. Replace `BUCKET_NAME` in the commands in this section with a unique bucket name.

```bash
AWS$ aws s3 mb s3://BUCKET_NAME
```

To prepare the application for deployment, use the `sam package` command.

```bash
AWS$ sam package \
    --output-template-file packaged.yaml \
    --s3-bucket BUCKET_NAME
```

The SAM CLI creates deployment packages, uploads them to the S3 bucket, and creates a new version of the template that refers to the artifacts in the bucket. 

To deploy the application, use the `sam deploy` command.

```bash
AWS$ sam deploy \
    --template-file packaged.yaml \
    --stack-name AWS \
    --capabilities CAPABILITY_IAM
```

After deployment is complete you can run the following command to retrieve the API Gateway Endpoint URL:

```bash
AWS$ aws cloudformation describe-stacks \
    --stack-name AWS \
    --query 'Stacks[].Outputs[?OutputKey==`NvaBareProxyApi`]' \
    --output table
``` 

## Use the SAM CLI to build and test locally

Build your application with the `sam build` command.

```bash
AWS$ sam build
```

The SAM CLI installs dependencies defined in `nva-bare-proxy/build.gradle`, creates a deployment package, and saves it in the `.aws-sam/build` folder.

Test a single function by invoking it directly with a test event. An event is a JSON document that represents the input that the function receives from the event source. Test events are included in the `events` folder in this project.

Run functions locally and invoke them with the `sam local invoke` command.

```bash
AWS$ sam local invoke BareAuthorityHandler --event events/event.json
```

The SAM CLI can also emulate your application's API. Use the `sam local start-api` to run the API locally on port 3000.

```bash
AWS$ sam local start-api
AWS$ curl http://localhost:3000/
```

The application expects two environment variables:
 * `BARE_HOST` defines the source of the Authority data (utvikle-a.bibsys.no for development, authority.bibsys.no for production)
 * `BARE_API_KEY` should be defined in the AWS SecretsManager and is needed to for update/PUT functionality

```yaml
      Environment: # More info about Env Vars: https://github.com/awslabs/serverless-application-model/blob/master/versions/2016-10-31.md#environment-object
        Variables:
          BARE_HOST: "{{resolve:ssm:bareHost:[VERSION]]}}"
          BARE_API_KEY: '{{resolve:secretsmanager:bareApiKey:SecretString}}'
```

The SAM CLI reads the application template to determine the API's routes and the functions that they invoke. The `Events` property on each function's definition includes the route and method for each path.

```yaml
  NvaBareFetchFunction:
    Type: AWS::Serverless::Function # More info about Function Resource: https://github.com/awslabs/serverless-application-model/blob/master/versions/2016-10-31.md#awsserverlessfunction
    Properties:
      Environment:
        Variables:
          AllowOrigin: !Sub
            - "${Domain}"
            - Domain: !Ref  CorsOrigin
      Handler: no.unit.nva.bare.FetchAuthorityHandler::handleRequest
      Runtime: java8
      MemorySize: 512
      Events:
        NvaBareFetchEvent:
          Type: Api # More info about API Event Source: https://github.com/awslabs/serverless-application-model/blob/master/versions/2016-10-31.md#api
          Properties:
            Auth:
              Authorizer: MyCognitoAuthorizer
            RestApiId: !Ref NvaBareProxyApi
            Path: /
            Method: post
            RequestModel:
              Model: Map<String, Object> # REQUIRED; must match the name of a model defined in the Models property of the AWS::Serverless::API
              Required: true # OPTIONAL; boolean

  NvaBareUpdateFunction:
    Type: AWS::Serverless::Function # More info about Function Resource: https://github.com/awslabs/serverless-application-model/blob/master/versions/2016-10-31.md#awsserverlessfunction
    Properties:
      Environment:
        Variables:
          AllowOrigin: !Sub
          - "${Domain}"
          - Domain: !Ref  CorsOrigin
          BARE_HOST: "utvikle-a.bibsys.no"
          BARE_API_KEY: '{{resolve:ssm:bareApiKey:1}}'
      Handler: no.unit.nva.bare.AddAuthorityIdentifierHandler::handleRequest
      Runtime: java8
      MemorySize: 512
      Events:
        NvaBareUpdateEvent:
          Type: Api # More info about API Event Source: https://github.com/awslabs/serverless-application-model/blob/master/versions/2016-10-31.md#api
          Properties:
            Auth:
              Authorizer: MyCognitoAuthorizer
            RestApiId: !Ref NvaBareProxyApi
            Path: /{scn}
            Method: put
            RequestModel:
              Model: Map<String, Object> # REQUIRED; must match the name of a model defined in the Models property of the AWS::Serverless::API
              Required: true # OPTIONAL; boolean
```

## Add a resource to your application
The application template uses AWS Serverless Application Model (AWS SAM) to define application resources. AWS SAM is an extension of AWS CloudFormation with a simpler syntax for configuring common serverless application resources such as functions, triggers, and APIs. For resources not included in [the SAM specification](https://github.com/awslabs/serverless-application-model/blob/master/versions/2016-10-31.md), you can use standard [AWS CloudFormation](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-template-resource-type-ref.html) resource types.

## Fetch, tail, and filter Lambda function logs

To simplify troubleshooting, SAM CLI has a command called `sam logs`. `sam logs` lets you fetch logs generated by your deployed Lambda function from the command line. In addition to printing the logs on the terminal, this command has several nifty features to help you quickly find the bug.

`NOTE`: This command works for all AWS Lambda functions; not just the ones you deploy using SAM.

```bash
AWS$ sam logs -n NvaBareProxy --stack-name AWS --tail
```

You can find more information and examples about filtering Lambda function logs in the [SAM CLI Documentation](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-logging.html).

## Unit tests

Tests are defined in the `NvaBareProxy/src/test` folder in this project.

```bash
AWS$ cd NvaBareProxy
NvaBareProxy$ gradle test
```

## Cleanup

To delete the sample application and the bucket that you created, use the AWS CLI.

```bash
AWS$ aws cloudformation delete-stack --stack-name AWS
AWS$ aws s3 rb s3://BUCKET_NAME
```

## Example

* GET to 

        /authority/?name=[name] 

        /authority/?feideid=[feideId] 
        
        /authority/?orcid=[orcId]
        
        /authority/?orgunitid=[orgUnitId]
        
     Response:
     ```json
        [
          {
            "name": "Moser, May-Britt",
            "systemControlNumber": "90517730",
            "feideid": [""],
            "orcid": [""],
            "orgunitid": [""],
            "birthDate": "1963-",
            "handle": ["http://hdl.handle.net/11250/1969546"]
          }
        ]
     ```



* POST to /authority/90517730 with body

    ```json
       {
            "name": "Moser, May-Britt",
            "scn": "90517730",
            "feideId": "may-britt.moser@ntnu.no",
            "orcId": "0000-0001-7884-3049",
            "birthDate": "1963-",
            "handle": "http://hdl.handle.net/11250/1969546"
       }
    ```
  or
  
    ```json
       {
          "orcid": "0000-0001-7884-3049"
       }
    ```
    or
    
      ```json
         {
            "orgunitid": "194.0.0.0"
         }
      ```
  (the body has to contain at least a value for at least one of the parameters: feideId, orcId.)
    
  Response:
   ```json
      [
        {
          "name": "Moser, May-Britt",
          "systemControlNumber": "90517730",
          "feideid": ["may-britt.moser@ntnu.no"],
          "orcid": ["0000-0001-7884-3049"],
          "orgunitid": ["194.0.0.0"],
          "birthDate": "1963-",
          "handle": ["http://hdl.handle.net/11250/1969546"]
        }
      ]
    ```
  
* POST to /authority/ with body

    ```json
       {
          "invertedname": "Unit, DotNo"
       }
    ```
    
    The 'invertedname' parameter value must contain a comma.
    
  Response:
   ```json
      [
        {
          "name": "Unit, DotNo",
          "systemControlNumber": "123456789",
          "feideid": [],
          "orcid": [],
          "orgunitid": [],
          "birthDate": "",
          "handle": []
        }
      ]
    ```

* POST to /authority/{scn}/identifiers/{qualifier}/add

    ```json
       {
          "identifier": "identifierValue"
       }
    ```

  Adds a qualified identifier to authority

* POST to /authority/{scn}/identifiers/{qualifier}/delete

    ```json
       {
          "identifier": "identifierValue"
       }
    ```

  Removes a qualified identifier from authority

* POST to /authority/{scn}/identifiers/{qualifier}/update

    ```json
       {
          "identifier": "identifierValue",
          "updatedIdentifier": "updatedIdentifierValue"
       }
    ```

  Updates a qualified identifier to a new value 

