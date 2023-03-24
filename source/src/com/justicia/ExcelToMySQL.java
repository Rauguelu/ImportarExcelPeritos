package com.justicia;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Collections;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelToMySQL {
	
	public static String capitalizeWords(String text) {
		StringBuilder capitalized = new StringBuilder();
		try {
		  String[] words = removeExtraSpaces(text.toLowerCase()).split(" ");
		  for (String word : words) {
		    capitalized.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
		  }
		} catch (Exception e) {
			System.out.println(e);
		}
		  return capitalized.toString().trim();
		}
	
	static String cutString(String text, int length) {
	    if (text.length() <= length) {
	        return text;
	    } else {
	        return text.substring(0, length);
	    }
	}
	public static String removeExtraSpaces(String text) {
	    return text.trim().replaceAll("\\s+", " ");
	}

	
	
	public static void main(String[] args) {
		
		// Para cargar masivamente los archivos excel es necesarios renombrarlos todos y utilizar numeración secuencial.
		// Por ejemplo llamarlos: "file(1).xls" , "file(2).xls"... 

		// Conexión a la base de datos MySQL
		String url = "jdbc:mysql://:3306/Peritos2013";
		String user = "root";
		String password = "gtjclm";

		// Nombres de las columnas de la tabla "Peritos".
		// Coinciden con las columnas de excel y las propiedades de la base de datos.

		String[] columnNames = { "APELLIDOS", "NOMBRE", "TELEFONO_FIJO", "TELEFONO_MOVIL", "FAX", "EMAIL", "DOMICILIO",
				"CODPOSTAL", "LOCALIDAD", "PROVINCIA", "NUMERO_PROFESIONAL", "ESPECIALIDADES", "ZONA_ACTUACION",
				"Justicia_Gratuita", "Justicia_Penal", "NOMBRE_AGRUPACION_PROFESIONAL", "TFNO_AGRUPACION_PROFESIONAL",
				"Email_AGRUPACION_PROFESIONAL" };

		try {
			// Conectar a la base de datos
			Connection con = DriverManager.getConnection(url, user, password);

			// Preparar la consulta SQL para insertar los datos en la tabla "Peritos"
			String sql = "INSERT INTO Peritos (" + String.join(", ", columnNames) + ") VALUES ("
					+ String.join(", ", Collections.nCopies(columnNames.length, "?")) + ")";

			System.out.println("Cadena SQL: " + sql);
			PreparedStatement pstmt = con.prepareStatement(sql);

			// Recorrer cada archivo Excel en la carpeta

			for (int i = 91; i <= 91; i++) {

				String filePath = "C:\\archivos_peritos\\"+"file_ (" + i + ").xls";
				
				// Abrir el archivo Excel
				FileInputStream inputStream = new FileInputStream(filePath);
				Workbook workbook = WorkbookFactory.create(inputStream);

				// Seleccionar la primera hoja del libro
				Sheet sheet = workbook.getSheetAt(0);
				// Recorrer cada fila de la hoja
				for (Row row : sheet) {
					// Saltarse la primera fila que contiene los títulos de las columnas
					if (row.getRowNum() == 0) {
						continue;
					}
					// Verificar si la fila tiene contenido
					boolean hasContent = false;
					DataFormatter formatter = new DataFormatter();
					for (Cell cell : row) {
						if (cell.getCellType() != Cell.CELL_TYPE_BLANK && formatter.formatCellValue(row.getCell(0)) != "") {
							hasContent = true;
							break;
						}
					}

					// Si la fila no tiene contenido, saltarse esta fila
					if (!hasContent) {
						continue;
					}

					// Establecer los valores para cada columna en la consulta SQL
					for (int j = 0; j < columnNames.length; j++) {
						
						String aux = ExcelToMySQL.cutString(formatter.formatCellValue(row.getCell(j)).trim(), 254);
							if(j==0) System.out.println(filePath + " - " + "FILA: "+ row.getRowNum() + " - Apellido: " +aux);
							if(j<2)aux = ExcelToMySQL.capitalizeWords(aux);
							 
									
						pstmt.setString(j + 1, aux);
						
					}
					
					// Ejecutar la consulta SQL
					pstmt.executeUpdate();
				}

				// Cerrar el archivo Excel
				inputStream.close();
			}

			// Cerrar la conexión a la base de datos
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

/* DESPUES DE ASIGNAR ExcelToMySQL , tenemos que lazar AsignaEspecialidades */
