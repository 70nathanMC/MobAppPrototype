package com.example.mobappprototype.Kotlin_Tutorials

fun main(){
    //Nullable Properties

    //val cannotBeNull1: String = null // Invalid
    val canBeNull2: String? = null // Valid

    //val cannotBeNull2: Int = null // Invalid
    val canBeNull1: Int? = null // Valid


    //Checking for Null
    val name: String? = null

    if (name != null && name.length > 0) {
        print("String length is ${name.length}")
    } else {
        print("String is empty.")
    }


    //Safe Operator


    //prevents an exception to be thrown when nullableString is null
    //entire expression will evaluate to null if nullableString is null
        //val nullableStringLength: Int? = nullableString?.length


    //ensures that at every step, if any part (like person, department, or head) is null,
    // the result will be null without trying to dereference a null reference.
        //val nullableDepartmentHead: String? = person?.department?.head?.name


    //Elvis Operator
        //val nullableString = "Hello"

    //If nullableString is null, the fallback value will be 0
        //val nonNullStringLength: Int = nullableString?.length ?: 0
    //If one of person, department, head, name is null, the fallback value will be ""
        //val nonNullDepartmentHead: String = person?.department?.head?.name ?: ""
    //similar to above, but using orEmpty() instead of : ""
        //val nonNullDepartmentHead: String = person?.department?.head?.name.orEmpty()


    //Safe Casts
    // as? operator
    // Will not throw ClassCastException
    //input is expected to be a Car object, but is a string instead, so will return null
        //val input: Any = "Hello"
        //val car: Car? = input as? Car


    //Static Fields
    //companion object: This block allows you to define staticField and staticMethod which
    //can be accessed through the class name (MyClass.staticField and MyClass.staticMethod())
//    class MyClass {
//        companion object {
//            val staticField = "I am a static field"
//            fun staticMethod() = "I am a static method"
//        }
//    }
//
//    fun main2() {
//        println(MyClass.staticField) // Accessing staticField without creating an instance of MyClass
//        println(MyClass.staticMethod()) // Calling staticMethod without creating an instance
//    }


    //Collections
    val numArray = arrayOf(1, 2, 3)
    val numList = listOf(1, 2, 3)
    val mutableNumList = mutableListOf(1, 2, 3)

    //Accessing
    val firstItem = numList[0]
    val firstItem2 = numList.first()
    val firstItem3 = numList.firstOrNull()


    //Maps
    val faceCards = mutableMapOf("Jack" to 11, "Queen" to 12, "King" to 13)
    val jackValue = faceCards["Jack"] // 11
    faceCards["Ace"] = 1


    //Mutability
    val immutableList = listOf(1, 2, 3)
    val mutableList = immutableList.toMutableList()

    val immutableMap = mapOf("Jack" to 11, "Queen" to 12, "King" to 13)
    val mutableMap = immutableMap.toMutableMap()


    //Iterating
    val myList = listOf(1,2,3)
    for (item in myList) {
        print(item)
    }

    myList.forEach {
        print(it)
    }

    myList.forEachIndexed { index, item ->
        print("Item at $index is: $item")
    }


    //Filtering and Searching

    //If any of the numbers on numList is even, it gets added to evenNumbers
    //Filtering a collection
    val evenNumbers = numList.filter { it % 2 == 0 }

    //Checking if any elements match the condition { it % 2 == 0 }
    val containsEven = numList.any { it % 2 == 0 }

    //returns true if no elements match the condition
    val containsNoEvens1 = numList.none { it % 2 == 0 }

    //returns true if all elements match the condition
    val containsNoEvens2 = numList.all { it % 2 == 1 }

    //returns the first element that matches the condition
    val firstEvenNumber: Int = numList.first { it % 2 == 0 }

    //returns the first element or null that matches the condition
    val firstEvenOrNull: Int? = numList.firstOrNull { it % 2 == 0 }

    //transforms objList via .map to map each object to a new format
    //map function transforms each element in the list by applying the given lambda expression to it
    data class MenuItem(val name: String, val detail: String)
    val objList = listOf(MenuItem("Burger", "5.99"), MenuItem("Fries", "2.99"))
    val fullMenu = objList.map { "${it.name} - $${it.detail}" }  // Output: ["Burger - $5.99", "Fries - $2.99"]



    //Named Parameters
    class Person(val name: String = "", age: Int = 0)

    // All valid
    val person1 = Person()
    val person2 = Person("Adam", 100)
    val person3 = Person(name = "Adam", age = 100)
    val person4 = Person(age = 100)
    val person5 = Person(age = 100, name = "Adam")




    //Parameters and Return Types
    fun printName() {
        print("Adam")
    }

    fun printName(person: Person) {
        print(person.name)
    }

    fun getGreeting1(person: Person): String {
        return "Hello, ${person.name}"
    }

    fun getGreeting2(person: Person): String = "Hello, ${person.name}"
    fun getGreeting3(person: Person) = "Hello, ${person.name}"


    //Default Parameters
    fun getGreeting4(person: Person, intro: String = "Hello,"): String {
        return "$intro ${person.name}"
    }

    // Returns "Hello, Adam"
    val hello = getGreeting4(Person("Adam"))

    // Returns "Welcome, Adam"
    val welcome = getGreeting4(Person("Adam"), "Welcome,")



    //Static Functions
    //companion object: Acts as a static-like container for class-level methods in Kotlin. In this case, it's used to provide a newInstance method.
    //newInstance method: A factory method for creating instances of the Fragment class, ensuring arguments are passed properly (a common Android pattern).
    //Creating a fragment: Instead of calling the constructor directly, you call Fragment.newInstance(args), which provides a clear,
    //centralized way to create and configure new instances of the Fragment.
//    class Fragment(val args: Bundle) {
//        companion object {
//            fun newInstance(args: Bundle): Fragment {
//                return Fragment(args)
//            }
//        }
//    }
//
//    val fragment = Fragment.newInstance(args)



    //Classes
    class Person2(val name: String, val age: Int)
    val adam = Person("Adam", 100)


    //Secondary Constructors
    //You can now create an object that only passes name via primary, or passes name and age due to secondary constructor
    class Person3(val name: String) {
        private var age: Int? = null

        constructor(name: String, age: Int) : this(name) {
            this.age = age
        }
    }

    // Above can be replaced with default params
    // sets age = null if nothing was provided
    class Person4(val name: String, val age: Int? = null)



    //Inheritance and Implementation
//    open class Vehicle            //open makes the Vehicle class inheritable
//    class Car : Vehicle()         //class Car inherits Vehicle
//
//    interface Runner {            //interface defines a set of methods that implementing classes must provide
//        fun run()
//    }
//
//    class Machine : Runner {      //implements Runner Interface
//        override fun run() {      //required to indicate that you are providing a specific implementation of a method defined in the interface
//            // ...
//        }
//    }



    //Control Flow

    //If Statements
//    if (someBoolean) {
//        doThing()
//    } else {
//        doOtherThing()
//    }


    //For Loops
//    for (i in 0..10) { } // 1 - 10
//    for (i in 0 until 10) // 1 - 9
//        (0..10).forEach { }
//    for (i in 0 until 10 step 2) // 0, 2, 4, 6, 8


    //When statements
//    when (direction) {
//        NORTH -> {
//            print("North")
//        }
//        SOUTH -> print("South")
//        EAST, WEST -> print("East or West")
//        "N/A" -> print("Unavailable")
//        else -> print("Invalid Direction")
//    }


    //While loops
//    while (x > 0) {
//        x--
//    }
//
//    do {
//        x--
//    } while (x > 0)




    //Destructing Declarations


    //Objects and Lists
//    val person = Person3("Adam", 100)
//    val (first_name, oldge) = person                    //Deconstruction, reassigning the values of person to first_name and oldge
//
//    val pair = Pair(1, 2)
//    val (first, second) = pair                          //Deconstruction, assigning the values of pair to first and second
//
//    val coordinates = arrayOf(1, 2, 3)
//    val (x, y, z) = coordinates                         //Deconstruction, assigning the values of coordinates to x, y, and z



    //componentN Functions
    class Person5(val name0: String, val age: Int) {
        operator fun component1(): String {
            return name0
        }

        operator fun component2(): Int {
            return age
        }
    }

    val person = Person5("Alice", 30)
    val (name0, age) = person  // Automatically calls component1() and component2()

    //instead of doing this:
//    val name = person.name
//    val age = person.age






}