## Under the hood
I will give you a short introduction into the internals of Typedapi and how it uses type-level computation to derive HTTP clients and servers.

**Note** When reading this document you have to keep in mind that Typedapi was created with the idea in mind to move most of the computations on the type-level.

### API Definition
You can define APIs by using the known dsl:

```Scala
val Api = := :> "user" :> Segment[String]('name) :> Get[User]
```

What's happening here is that Typedapi creates a `FinalCons[H]` with `H <: shapeless.HList` representing the given API. 
When you inspect the `H` for the given example it will look like the following:

```Scala
val findW = Witness("find")
val nameW = Witness('name)

type H = GetElement[User] :: SegmentParam[nameW.T, String] :: PathElement[findW.T] :: HNil
```

`FinalCons` itself doesn't store any value. It is just a carrier (helper value) for `H` as we are not able to work directly with types as we do with values in Scala.

#### How does it work?
When calling `:=` you create an empty API type carrier. Everytime you use `:>` you add a new element to the type. The actual values you supply are just there to overcome the limitations of Scala, e.g. we cannot simply define a singleton type from a String on the type-level like this `type findW = "find"`. We have to create a singleton typed value (Witness) from that literal and extract the type.

### API Transformation
After we moved the API to the type-level we need to extract the expected input which we can use later to determine the client functions. We do that by transforming `H <: HList` to `(El <: HList, KIn <: HList, VIn <: HList, A)` with:
  - `El`, API elements
  - `KIn`, key types of expected input
  - `VIn`, value types of expected input
  - `A`, expected output
  
For our example this will look like the following:
  
```Scala
type El = findW.T :: SegmentInput :: GetCall :: HNil
type KIn = nameW.T :: HNil
type VIn = String :: HNil
type A  = User
```

`El` doesn't carry type information anylonger about input or output types. It just defines what elements are within the API. On the otherhand, `KIn` and `VIn` describe the input types and their names without caring about the actual API elements.

#### How does it work?
The type-class `ApiTransformer` walks through the API and maps every element to `El` and `KIn`/`VIn` if it is an input element. To do so Typedapi implements `TypelevelFoldLeft` which does the same as the usual `foldLeft` over `List` structures but operates solely on a `HList` type. No instance is given. What happens is, that Typedapi folds `H` and thereby creates the expected outcome `(El, KIn, VIn, A)`.

All the steps we saw till now are similar for the client and server side. This is not true for the following parts and that is why we will distingiush the two from now on.

## Client side
### API Compiler to generate request data
The last step generates a function `VIn => RequestData[R, D <: HList]` which gets all expected inputs as defined by `VIn` and maps them to `type RequestData[R, D <: HList] = FieldType[R, D] :: HNil`. Here `R` represents the method and `D` is a `HList` composed of:
  - uri (`List[String]`)
  - query (`Map[String, List[String]]`)
  - header (`Map[String, String]`)
  - optional request body (`Bd`)

The type-class `RequestDataBuilder` generates this function from `Api`. Now we need to make the call more "function-like". So, instead of passing a `HList` we want to pass distinct parameters. To achieve that we leverage `shapeless.ops.function.FnFromProduct`.

### Execute a request
After creating a function to generate request data we now need to execute a request. This is done by the `ApiRequest` implementations provided by the different HTTP frameworks like *http4s* or *akka-http*.

<... will be continued>
