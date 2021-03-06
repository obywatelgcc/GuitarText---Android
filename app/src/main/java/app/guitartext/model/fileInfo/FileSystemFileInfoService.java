package app.guitartext.model.fileInfo;

import android.text.TextUtils;

import com.annimon.stream.Optional;
import com.google.common.io.Files;
import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by obywatel on 10.03.2017.
 * Modified by
 */

public class FileSystemFileInfoService implements FileInfoService
{
	private static final Logger logger = LoggerManager.getLogger();

	private static final FileInfo ROOT_LOCATION = new FileInfo(0, true, "/", "/");
	private static final String HIDDEN_FILE_PREFIX = ".";
	private static final char SEPARATOR = '/';

	@Inject
	public FileSystemFileInfoService()
	{
	}

	@Override
	public FileInfo getRootLocation()
	{
		return FileSystemFileInfoService.ROOT_LOCATION;
	}

	@Override
	public FileInfo getValidateDirectory(FileInfo fileInfo)
	{
		if(fileInfo == null) return ROOT_LOCATION;

		File file = new File(fileInfo.getPath());
		if(!file.exists())
		{
			return ROOT_LOCATION;
		}

		if(!file.isDirectory())
		{
			file = file.getParentFile();
			return new FileInfo(0, true, file.getAbsolutePath(), file.getName());
		}

		return fileInfo;
	}

	@Override
	public List<FileInfo> getLocationContent(FileInfo location)
	{
		File file = new File(location.getPath());
		File[] subFiles = file.listFiles();

		List<FileInfo> locationContent;
		if(subFiles == null || subFiles.length == 0)
		{
			return Collections.emptyList();
		}

		locationContent = new ArrayList<>(subFiles.length);
		int i = 0;
		for(File subFile : subFiles)
		{
			if(subFile.getName().startsWith(HIDDEN_FILE_PREFIX)) continue;
			locationContent.add(createFileInfo(i, subFile));
			i++;
		}

		return locationContent;
	}

	@Override
	public List<FileInfo> getFileWithAncestors(FileInfo fileInfo)
	{
		LinkedList<FileInfo> pathItemList = new LinkedList<>();

		File file = new File(fileInfo.getPath());

		FileInfo info = createFileInfo(0, file);

		while(file != null && !TextUtils.isEmpty(info.getName()))
		{
			pathItemList.addFirst(info);
			file = file.getParentFile();
			info = createFileInfo(0, file);
		}

		return pathItemList;
	}

	@Override
	public Optional<String> readFile(FileInfo fileInfo)
	{
		try
		{
			return Optional.ofNullable(
					Files.toString(
							new File(fileInfo.getPath()),
							Charset.forName("UTF-8")));
		} catch(IOException e)
		{
			logger.e(e);
		}
		return Optional.empty();
	}

	@Override
	public Optional<List<String>> readFileLines(FileInfo fileInfo)
	{
		try
		{
			return Optional.ofNullable(
					Files.readLines(
							new File(fileInfo.getPath()),
							Charset.forName("UTF-8")));
		} catch(IOException e)
		{
			logger.e(e);
		}
		return Optional.empty();
	}

	@Override
	public Optional<FileInfo> createFileFromPath(String path)
	{
		File file = new File(path);
		if(file.exists())
			return Optional.of(createFileInfo(0, file));
		return Optional.empty();
	}

	private FileInfo createFileInfo(int position, File file)
	{
		if(file == null) return new FileInfo(position, false, "", "");
		return new FileInfo(position, file.isDirectory(), file.getAbsolutePath(), getNameForPath(file.getAbsolutePath()));
	}

	private String getNameForPath(String path)
	{
		int index = path.lastIndexOf(SEPARATOR);
		if(index < 0) return path;
		if(path.length() == 1) return path;
		return path.substring(index + 1);
	}
}
