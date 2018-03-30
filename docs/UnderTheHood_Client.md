## Under the hood
I will give you a short introduction in how Typedapi works under the hood and how it uses type-level computation to derive HTTP clients.

When reading this document you have to keep in mind that Typedapi was created with the idea in mind to move most of the computations
on the type-level. Thus, you will encounter just a small number of actual values.

### Api Definition
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

`FinalCons` itself doesn't store any value. It is just a carrier for `H` as we are not able directly work with types as we do with
values in Scala.

#### How does it work?
When calling `:=` you create an empty API type carrier. Everytime you use `:>` you add a new element to the type. The actual values
you supply are just there to overcome the limitations of Scala, e.g. we cannot simply define a singleton type from a String on the
type-level like this `type findW = "find"`. We have to create a singleton typed value (Witness) from that literal and extract the
type.

### Api Transformation
After we moved the API to the type-level we need to extract the expected input which we can use later on to determine the client
functions. We do that by transforming `H <: HList` to a type triple `(El <: HList, In <: HList, A)` with:
  - `El`, API elements
  - `In`, expected input
  - `A`, expected output
  
For our example this will look like the following:
  
```Scala
type El = findW.T :: SegmentInput :: GetCall :: HNil
type In = FieldType[nameW.T, String] :: HNil
type A  = User
```

`El` doesn't carry type information anylonger about input or output types. It just defines what elements are within the API. On the
otherhand, `In` describes the input types and their names without caring about the actual API elements.

#### How das it work?
The typeclass `ApiTransformer` walks through the API and maps every element to `El` and `In` if it is an input element. To do so 
Typeapi implements `TypelevelFoldLeft` which does the same as the usual `foldLeft` of `List` structures but operates solely on 
a `HList` type. No instance is given. What happens is, that Typedapi folds `H` and thereby creates the type triple `(El, In, A)`.
