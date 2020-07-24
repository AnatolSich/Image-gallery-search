<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>

<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>Found pictures</title>
  <link rel="stylesheet" type="text/css" href="${contextPath}/resources/css/style.css">
</head>

<body>
<div>
  <table>
    <thead>
    <th>ID</th>
    <th>Author</th>
    <th>Camera</th>
    <th>Cropped_picture</th>
    <th>Full_picture</th>
    </thead>
    <c:forEach items="${pictures}" var="pic">
      <tr>
        <td>${pic.id}</td>
        <td>${pic.author}</td>
        <td>${pic.camera}</td>
        <td>${pic.cropped_picture}</td>
        <td>${pic.full_picture}</td>
      </tr>
    </c:forEach>
  </table>
</div>
</body>
</html>