# Chapter 2 — How to structure a web application with the MVC pattern

Text extracted from `Chapter 02 slides.pptx` (Murach's Java Servlets/JSP, 3rd Ed.).
Every slide was a picture, so the content below comes from the vector text inside
the slide images.

## Objectives (knowledge)

1. Describe the Model 1 pattern.
2. Describe the Model 2 (MVC) pattern.
3. Explain how the MVC pattern can improve application development.
4. Distinguish between the HTML and CSS for a web page.
5. Distinguish between the code for a servlet and a JSP.
6. Explain why you typically use both servlets and JSPs in a Java web application.
7. Describe the purpose of the deployment descriptor in a web application.
8. Describe the purpose of a JavaBean within a web application.

## Concepts and terminology

- The **Model 1 pattern** uses JSPs to handle all of the processing and presentation
  for the application.
- The **Model 2 pattern** separates the code into a model, a view, and a controller.
  As a result, it's also known as the **Model-View-Controller (MVC)** pattern.
- The **model** consists of business objects like the `User` object.
- The **view** consists of HTML pages and JSPs.
- The **controller** consists of servlets.
- The **data access layer** consists of classes like the `UserDB` class that read and
  write business objects like the `User` object to and from the data store.
- Try to construct each layer so it's as independent as possible.

## Types of files in the MVC pattern

- An **HTML** file contains tags that define the content of the web page.
- A **CSS** (Cascading Style Sheet) file contains the formatting for the web pages.
- **Servlets** contain Java code for a web application. When a servlet controls the
  flow of the application, it's known as a *controller*.
- The **web.xml** file, or *deployment descriptor* (DD), describes how the web
  application should be configured when it's deployed.
- A **JavaBean**, or bean, is a Java class that (1) provides a zero-argument
  constructor, (2) provides get and set methods for all of its instance variables,
  and (3) implements the `Serializable` or `Externalizable` interface.
- A **JavaServer Page (JSP)** consists of special Java tags such as Expression
  Language (EL) tags that are embedded within HTML code. An EL tag begins with a
  dollar sign (`$`).

## Slide-by-slide map

| Slide | Content | Implemented in |
|-------|---------|----------------|
| 1–2   | Title, objectives | — |
| 3     | The Model 1 pattern (diagram) | — |
| 4     | The Model 2 (MVC) pattern (diagram) | — |
| 5     | Concepts and terminology | above |
| 6     | The HTML page that gets data from the user (screenshot) | `index.html` |
| 7     | The JSP that displays the data (screenshot) | `thanks.jsp` |
| 8–9   | The index.html file | `src/main/webapp/index.html` |
| 10    | The main.css file | `src/main/webapp/styles/main.css` |
| 11–13 | The EmailListServlet class | `murach/email/EmailListServlet.java` |
| 14–15 | The web.xml file | `src/main/webapp/WEB-INF/web.xml` |
| 16–17 | The User class | `murach/business/User.java` |
| 18–19 | The thanks.jsp file | `src/main/webapp/thanks.jsp` |
| 20    | Types of files in the MVC pattern | above |

## Request flow

```
Browser  --POST /ch02email/emailList (action=add)-->  EmailListServlet   (controller)
                                                          |
                                          new User(...)   v
                                                       User             (model)
                                                          |
                                          UserDB.insert(user)
                                                          v
                                                     data store
                                                          |
                        request.setAttribute("user", user)
                                                          v
                                          forward to /thanks.jsp        (view)
```
